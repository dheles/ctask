/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.ctask.fixity;

import java.io.IOException;
import java.sql.SQLException;

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
            try {
                for (Bundle bundle : item.getBundles()) {
                    for (Bitstream bs : bundle.getBitstreams()) {
                        try {
                            bs.retrieve();
                            String result = "Retreived bitstream in item: " + item.getHandle() +
                                      " . Bitstream: '" + bs.getName() + "' (seqId: " + bs.getSequenceID() + ")";
                            log.debug(result);
                        } catch (Exception e) {
                            String result = "Unable to retreive bitstream in item: " + item.getHandle() +
                                      " . Bitstream: '" + bs.getName() + "' (seqId: " + bs.getSequenceID() + ")" +
                                      " error: " + e.getMessage();
                            report(result);
                            setResult(result);
                            log.error(result);
                            return CURATE_SKIP;

                            // throw new IOException("Exception retreiving bitstream: " + e.getMessage());
                        }
                        String compCs = Utils.checksum(bs.retrieve(), bs.getChecksumAlgorithm());
                        if (! compCs.equals(bs.getChecksum())) {
                            String result = "Checksum discrepancy in item: " + item.getHandle() +
                                      " for bitstream: '" + bs.getName() + "' (seqId: " + bs.getSequenceID() + ")" +
                                      " ingest: " + bs.getChecksum() + " current: " + compCs;
                            report(result);
                            setResult(result);
                            log.error(result);
                            // NOTE: returning skip, rather than fail allows the task to continue
                            // return CURATE_FAIL;
                            return CURATE_SKIP;
                        }
                    }
                }
            } catch (AuthorizeException authE) {
                throw new IOException("AuthorizeException: " + authE.getMessage());
            } catch (SQLException sqlE) {
                throw new IOException("SQLException: " + sqlE.getMessage());
            } catch (Exception e) {
                throw new IOException("Unknown Exception: " + e.getMessage());
            }
            setResult("All bitstream checksums agree in item: " + item.getHandle());
            return CURATE_SUCCESS;
        } else {
            return CURATE_SKIP;
        }
    }
}
