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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Bitstream;
import org.dspace.content.Bundle;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.core.Constants;
import org.dspace.curate.AbstractCurationTask;
import org.dspace.curate.Suspendable;
import org.dspace.curate.Utils;

import static org.dspace.curate.Curator.*;

/**
 * Based on richardrodgers' CheckChecksum,
 * CheckFixity confirms the presence of each selected bitstream.
 * It then computes a checksum for each selected bitstream
 * and compares it to the stored ingest-time calculated value.
 * Task succeeds if all bitstreams are present & checksums agree, else fails.
 * TODO: add logging
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
                    for (Bitstream bs : bundle.getBitstreams()) {
                        try {
                            bs.retrieve();
                            String result = "Retreived bitstream in item: " + item.getHandle() +
                                      " . Bitstream: '" + bs.getName() + "' (seqId: " + bs.getSequenceID() + ")";
                            log.debug(result);

                            // since we can retreive the bitstream, go ahead and check its checksum
                            String compCs = Utils.checksum(bs.retrieve(), bs.getChecksumAlgorithm());
                            if (! compCs.equals(bs.getChecksum())) {
                                result = "Checksum discrepancy in item: " + item.getHandle() +
                                          " for bitstream: '" + bs.getName() + "' (seqId: " + bs.getSequenceID() + ")" +
                                          " ingest: " + bs.getChecksum() + " current: " + compCs;
                                // report(result);
                                // setResult(result);
                                log.error(result);
                                results.add(result);
                            }
                        } catch (FileNotFoundException e) {
                            String result = "Unable to retreive bitstream in item: " + item.getHandle() +
                                      " . Bitstream: '" + bs.getName() + "' (seqId: " + bs.getSequenceID() + ")" +
                                      " error: " + e.getMessage();
                            // report(result);
                            // setResult(result);
                            log.error(result);
                            results.add(result);
                            // return CURATE_SKIP;

                            // throw new IOException("Exception retreiving bitstream: " + e.getMessage());
                        }
                    }
                }
            } catch (AuthorizeException authE) {
                results.add("AuthorizeException: " + authE.getMessage());
                status = CURATE_ERROR;
                throw new IOException("AuthorizeException: " + authE.getMessage());
            } catch (SQLException sqlE) {
                results.add("SQLException: " + sqlE.getMessage());
                status = CURATE_ERROR;
                throw new IOException("SQLException: " + sqlE.getMessage());
            }
            // results.add("All bitstream checksums agree in item: " + item.getHandle());
            // status =  CURATE_SUCCESS;
        } else {
            status = CURATE_SKIP;
        }

        processResults();
        return status;
    }

    private void processResults() throws IOException
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Translation report: \n----------------\n");
        for(String result : results)
        {
            sb.append(result).append("\n");
        }
        setResult(sb.toString());
        report(sb.toString());

    }
}
