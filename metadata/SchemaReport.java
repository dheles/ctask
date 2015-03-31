/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.ctask.metadata;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.dspace.authorize.AuthorizeManager;
import org.dspace.content.Bitstream;
import org.dspace.content.Bundle;
import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.content.ItemIterator;
import org.dspace.content.MetadataField;
import org.dspace.content.MetadataSchema;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.curate.AbstractCurationTask;
import org.dspace.curate.Curator;
import org.dspace.curate.Distributive;

import com.google.gson.Gson;


/**
 * Dump the metadata registry to a JSON file
 * /opt/dspace/bin/dspace curate -i all -t metadatareg -r - > metadatareg.json
 * @author twb27
 */
@Distributive
public class SchemaReport extends AbstractCurationTask
{
    
    @Override
    public int perform(DSpaceObject dso) throws IOException
    {
        try {
            return perform(Curator.curationContext(),"");
        } catch (SQLException e) {
            return Curator.CURATE_FAIL;
        }
    }
    
    @Override
    public int perform(Context ctx, String id) throws IOException
    {
        try {
            List<Schema> mySchemas = new ArrayList<Schema>();
            MetadataSchema[] schemas = MetadataSchema.findAll(Curator.curationContext());
            for(MetadataSchema schema: schemas) {
                Schema mySchema = new Schema(schema.getName(), schema.getNamespace());
                mySchemas.add(mySchema);
                MetadataField[] fields = MetadataField.findAllInSchema(Curator.curationContext(), schema.getSchemaID());
                for(MetadataField field: fields){
                    String fname = Field.makeName(mySchema.prefix(), field.getElement(), field.getQualifier());
                    mySchema.addField(fname, field.getElement(), field.getQualifier(), field.getScopeNote());
                }
            }
            Gson gson = new Gson();
            String s = gson.toJson(mySchemas);
            report(s);
            setResult(s);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Curator.CURATE_SUCCESS;
    }

}
