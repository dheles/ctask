# Metadata Curation Tasks #

## SchemaReport ##

This curation task provides a JSON dump of the metadata registry.

``[DSPACE-DIR]/bin/dspace curate -i all -t metadatareg -r - > metadatareg.json``

This form of the metadata registry is intended for consumption by the FileAnalyzer tool.  This tool automates the creation of DSpace ingest packages.

See https://github.com/Georgetown-University-Libraries/File-Analyzer/releases/tag/v3.0.1 for more details.

### Add Metadata Registry Parameter to the DSpace Ingest Folder Create and Ingest Folder Validate Tasks

![image](https://cloud.githubusercontent.com/assets/1111057/6398698/2c6b946a-bda2-11e4-932c-4351c6c8e6a6.png)

![image](https://cloud.githubusercontent.com/assets/1111057/6398683/186d2b04-bda2-11e4-8b2c-efdc92b4e5c7.png)

### Flag Metadata Fields not found in the Metadata Registry

![image](https://cloud.githubusercontent.com/assets/1111057/6398724/517aa4c6-bda2-11e4-8ddd-421e3d15a126.png)

![image](https://cloud.githubusercontent.com/assets/1111057/6398728/60b8b02c-bda2-11e4-870a-9d75df799995.png)

### Sample Metadata Registry Format

![image](https://cloud.githubusercontent.com/assets/1111057/6398762/ade74b92-bda2-11e4-8ff4-8c455af6813d.png)

## ExceptionReport ##

Report on exceptional use cases within a DSpace repository.

``[DSPACE-DIR]/bin/dspace curate -t exception -i all -r -``

Sample Output

    TOTAL ITEMS (53397)
    Is Private Item (1)
        10822.3/560020
    Is Withdrawn Item (44, 25 showing)
        10822/546771
        10822/547291
        10822/547537
        10822/548172
        10822/548444
        10822/548446
        10822/548447
        10822/548448
        10822/548604
        10822/548610
        10822/548627
        10822/549379
        10822/548327
        10822/520106
        10822/534689
        10822/511752
        10822/512368
        10822/514063
        10822/547032
        10822/547034
        10822/517546
        10822/547784
        10822/547992
        10822/538371
        10822/548445
    Has Restricted Access (0)
    Has Original Bitstream with Restricted Access (0)
    Has No Original Bitstreams (45835, 25 showing)
        10822.3/560130
        10822.3/560021
        10822.3/560019
        10822.3/560028
        10822.3/560123
        10822/514310
        10822/504862
        10822/514311
        10822/515644
        10822/504863
        10822/504864
        10822/515645
        10822/504865
        10822/542820
        10822/514314
        10822/504866
        10822/516443
        10822/515648
        10822/504868
        10822/542821
        10822/528258
        10822/516444
        10822/515649
        10822/504870
        10822/515650
    Has Multiple Original Bitstreams (51, 25 showing)
        10822.3/560017
        10822.3/559974
        10822.3/559973
        10822.3/559972
        10822.3/559971
        10822.3/559970
        10822.3/559969
        10822.3/560027
        10822.3/560026
        10822.3/560020
        10822/551516
        10822/550893
        10822/550896
        10822/550889
        10822/550888
        10822/550897
        10822/550894
        10822/550892
        10822/559297
        10822/559298
        10822/559299
        10822/559300
        10822/559311
        10822/559309
        10822/559307
    Has Unexpected Bitstream Types (705, 25 showing)
        10822.3/560127 image/png
        10822.3/560126 image/png
        10822.3/559974 application/octet-stream
        10822.3/559973 application/vnd.openxmlformats-officedocument.wordprocessingml.document
        10822.3/559972 application/vnd.openxmlformats-officedocument.spreadsheetml.sheet
        10822.3/559970 application/octet-stream
        10822.3/559978 application/msword
        10822.3/560010 image/png
        10822.3/560011 image/png
        10822/558692 text/html
        10822/558701 text/html
        10822/558693 text/html
        10822/558694 text/html
        10822/558708 text/html
        10822/558686 text/html
        10822/558702 text/html
        10822/558695 text/html
        10822/558687 text/html
        10822/558688 text/html
        10822/558696 text/html
        10822/558713 text/html
        10822/558689 text/html
        10822/558703 text/html
        10822/558697 text/html
        10822/558690 text/html


# Configuration: Add the following to config/modules/curate.cfg

    plugin.named.org.dspace.curate.CurationTask = \
    
        org.dspace.ctask.metadata.ExceptionReport = exception, \
        org.dspace.ctask.metadata.SchemaReport = metadatareg, \
        org.dspace.ctask.general.MetadataValueLinkChecker = checklinks
    ui.tasknames = \
         exception = DigitalGeorgetown Exception Report, \
         metadatareg = Metadata Registry Report, \
