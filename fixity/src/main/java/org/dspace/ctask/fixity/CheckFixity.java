/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.ctask.fixity;

import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.*;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.BitstreamService;
import org.dspace.core.Constants;
import org.dspace.curate.AbstractCurationTask;
import org.dspace.curate.Curator;
import org.dspace.curate.Suspendable;
import org.dspace.curate.Utils;

import static org.dspace.curate.Curator.*;

/**
 * Based on richardrodgers' CheckChecksum,
 * CheckFixity confirms the presence of each selected bitstream.
 * It then computes a checksum for each selected bitstream
 * and compares it to the stored ingest-time calculated value.
 * Task succeeds if all bitstreams are present & checksums agree.
 * Unfortunately, returning fail will stop the task, so it never fails.
 *
 * @author richardrodgers
 * @author dheles
 */

@Suspendable(invoked=Invoked.INTERACTIVE)
public class CheckFixity extends AbstractCurationTask
{
    /** log4j category */
    private static final Logger log = Logger.getLogger(CheckFixity.class);

    private List<String> results = new ArrayList<String>();

    int status = CURATE_UNSET;

    protected BitstreamService bitstreamService;

    @Override
    public void init(Curator curator, String taskId) throws IOException
    {
        super.init(curator, taskId);
        bitstreamService = ContentServiceFactory.getInstance().getBitstreamService();

        // clear our results list from previous tasks
        results.clear();
    }

    /**
     * Perform the curation task upon passed DSO
     *
     * @param dso the DSpace object
     * @throws IOException
     */
    @Override
    public int perform(DSpaceObject dso) throws IOException  {
        if (dso.getType() == Constants.ITEM) {
            Item item = (Item)dso;

            status = CURATE_SUCCESS;

            try {
                for (Bundle bundle : item.getBundles()) {
                    for (Bitstream bitstream : bundle.getBitstreams()) {
                        try {
                            InputStream inputstream = bitstreamService.retrieve(Curator.curationContext(), bitstream);
                            String result = "Retreived bitstream in item: " + item.getHandle() +
                                      " . Bitstream: '" + bitstream.getName() + "' (seqId: " + bitstream.getSequenceID() + ")";
                            log.debug(result);

                            // since we can retreive the bitstream, go ahead and check its checksum
                            String compCs = Utils.checksum(inputstream, bitstream.getChecksumAlgorithm());
                            if (! compCs.equals(bitstream.getChecksum())) {
                                result = "Checksum discrepancy in item: " + item.getHandle() +
                                          " for bitstream: '" + bitstream.getName() + "' (seqId: " + bitstream.getSequenceID() + ")" +
                                          " ingest: " + bitstream.getChecksum() + " current: " + compCs;
                                log.error(result);
                                results.add(result);
                                status = CURATE_SKIP;
                            }
                            inputstream.close();
                        } catch (FileNotFoundException e) {
                            String result = "Unable to retreive bitstream in item: " + item.getHandle() +
                                      " . Bitstream: '" + bitstream.getName() + "' (seqId: " + bitstream.getSequenceID() + ")" +
                                      " error: " + e.getMessage();
                            log.error(result);
                            results.add(result);
                            status = CURATE_SKIP;
                        }
                    }
                }
            } catch (AuthorizeException authE) {
                throw new IOException("AuthorizeException: " + authE.getMessage());
            } catch (SQLException sqlE) {
                throw new IOException("SQLException: " + sqlE.getMessage());
            }
            if (status == CURATE_SUCCESS) {
              String result = "All bitstream checksums agree in item: " + item.getHandle();
              log.debug(result);
              results.add(result);
            }
        } else {
            status = CURATE_SKIP;
        }

        processResults();
        return status;
    }

    private void processResults() throws IOException
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Fixity report: \n----------------\n");
        for(String result : results)
        {
            sb.append(result).append("\n");
        }
        setResult(sb.toString());
        report(sb.toString());
    }
}
