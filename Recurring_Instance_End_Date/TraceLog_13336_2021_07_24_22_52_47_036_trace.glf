FILE_TYPE:DAAA96DE-B0FB-4c6e-AF7B-A445F5BF9BE2
ENCODING:UTF-8
RECORD_SEPARATOR:30
COLUMN_SEPARATOR:124
ESC_CHARACTER:27
COLUMNS:Location|Guid|Time|Tzone|Trace|Log|Importance|Severity|Exception|DeviceName|ProcessID|ThreadID|ThreadName|ScopeTag|MajorTick|MinorTick|MajorDepth|MinorDepth|RootName|RootID|CallerName|CallerID|CalleeName|CalleeID|ActionID|DSRRootContextID|DSRTransaction|DSRConnection|DSRCounter|User|ArchitectComponent|DeveloperComponent|Administrator|Unit|CSNComponent|Text
SEVERITY_MAP: |None| |Success|W|Warning|E|Error|A|Assertion
HEADER_END
|E86AE7D831DC419EBBEF121D4861B01E0|2021 07 24 22:52:47.122|-0400|Error| |==|E| |TraceLog|13336|   1|main            | ||||||||||||||||||||com.crystaldecisions.enterprise.ocaframework.WireOb3Unpacker||Assertion failed: initialize(): invalid xrl,EBI Support Reports
java.lang.AssertionError
	at com.businessobjects.foundation.logging.log4j.Log4jLogger.assertTrue(Log4jLogger.java:52)
	at com.crystaldecisions.enterprise.ocaframework.WireOb3Unpacker.initialize(WireOb3Unpacker.java:127)
	at com.crystaldecisions.celib.properties.Property.getPropertyBag(Property.java:354)
	at com.crystaldecisions.celib.properties.PropertyBag.getPropertyBag(PropertyBag.java:425)
	at com.crystaldecisions.sdk.properties.internal.SDKPropertyBag.getProperties(SDKPropertyBag.java:148)
	at com.crystaldecisions.sdk.properties.internal.SDKPropertyBag.getProperties(SDKPropertyBag.java:144)
	at recurring.Recurring.main(Recurring.java:95)

