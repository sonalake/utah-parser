# Utah Parser [![Build Status](https://travis-ci.org/sonalake/utah-parser.png?branch=master)](https://travis-ci.org/sonalake/utah-parser) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.sonalake/utah-parser/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.sonalake/utah-parser) [![Coverage Status](https://coveralls.io/repos/github/sonalake/utah-parser/badge.svg?branch=master)](https://coveralls.io/github/sonalake/utah-parser?branch=master)
**A library for parsing semi-structured text files**

The purpose of this library is to allow the parsing of semi-structured text files, that meet the requirements that:

1. The file is to be parsed into a list of record, where each record is a simple name->value pair 
(so, none of the fields are lists or anything complex).
1. Every record is delimited by either one of set of *single-line* regular expressions, or the end of the file.
1. Every value in the record can be identified by a *multiple-line* regular expression where although the regex may have multiple groups, 
the value comes from a **single** group.

What we were looking for was something that could handle what [textfsm](https://github.com/google/textfsm) could do, but in java.
This is using jdk7 for operational reasons, but there's a lot going on in here that would be much easier to handle with lambdas. 

## Configuring the parser

The parser uses a file such as this:

		<config>
    		<!-- Each record will end with this -->
    		<delim><![CDATA[====================================================]]></delim>
    		<searches>
    			<!-- Some rules for finding text, to make the values a little easier below -->
    			<search id="numbers"><![CDATA[(\d+)]]></search>
    			<search id="stringToEOL"><![CDATA[(.+?)[\n\r]]]></search>
    			<search id="datePattern"><![CDATA[\d{2}\/\d{2}\/\d{4} \d{2}:\d{2}:\d{2} [AP]M]]></search>
    		</searches>
    		<values>
    			<!-- some fields that are reusing the patterns above -->
    			<value id="numberField"><![CDATA[Some ID: {numbers}]]></value>
    			<value id="stringFieldA"><![CDATA[Some text: {stringToEOL}]]></value>
    			<value id="stringFieldB"><![CDATA[Some other text: {stringToEOL}]]></value>
    			<value id="someOtherNumberField"><![CDATA[Some other ID: {numbers}]]></value>
    			<!-- 
    			in this case we have two dates on the same line, and we want to pull
    			values from different places
    			-->
    			<value id="startOfInterval"><![CDATA[Interval:\s*({datePattern})]]></value>
    			<value id="endOfInterval"><![CDATA[Interval:\s*{datePattern} - ({datePattern})]]></value>
    		</values>
    	</config>

Where the intention is to use the **delim** to split a file into records, and then use regular expressions, defined in
the **values** section to pull out fields and values. The **searches** section is optional, but it allows you to define
a regular expression once, and then reuse it multiple times.

**NOTE:** the delimiters are applied **PER LINE**. That is, the file is processed a line at a time, and if a line matches 
the regular expression then the text before that line is parsed for record values. All of the processed text is then ignored
on the next iteration. If the file finishes before a delimiter is found then all the remaining text is treated as a record.

e.g. In the above sample there is a *search* called **numbers**, that contains **(\d+)** that is, find a solid string of numbers.
 It's used in a few fields, one of which is **numberField**, which uses the *search* as 
     Some ID: {numbers}
     
 The *curly-braces* are used to do the substitution, and multiple substitutions can be used. A *search* can even contain 
 other searches, however, they are applied **once** and in the order in which they appear in the config file.

 If **XXXXX** is the regex derived from the above configuration for a field, then the regex that is *actually* used is this: 
 
     /.*?XXXXX.*/s

i.e. each regex is applied to the **entire** record, and the first match is used.

## Sample file with a simple delimiter

Assuming the above configuration, if the library were to process this file here:

	This is a semi-structured record
	Some ID: 123
	Some other ID: 456
	Some text: what'll I do?
	Some other text: without you?
	Interval:
	10/22/2015 12:00:00 AM - 12/31/2015 11:59:00 PM
	====================================================
	This is another semi-structured record
	Some ID: 987
	Some other ID: 654
	Some text: some fields are missing!
	Interval:
	10/22/2015 12:00:00 AM

Then two records would be returned:

	[
		{
			'numberField' : '123', 
			'someOtherNumberField' : '456', 
			'stringFieldA' : 'what'll I do?', 
			'stringFieldB' : 'without you?', 
			'startOfInterval' : '10/22/2015 12:00:00 AM',
			'endOfInterval' : '12/31/2015 11:59:00 PM',
		},
		{
			'numberField' : '987', 
			'someOtherNumberField' : '654', 
			'stringFieldA' : 'some fields are missing!', 
			'startOfInterval' : '10/22/2015 12:00:00 AM'
		}
	]
 
**NOTE:** you will observe that the results are a simple List<Map<String, String>>
and that any fields with no values in the record will **NOT** be present in the results

## Sample file with a record per line where some header values are included in every record

In this sample, there is a header with some values that we want to include in every record

	BGP router identifier 192.0.2.1, local AS number 65551

	Neighbor        AS            MsgRcvd  MsgSent     TblVer  InQ  OutQ Up/Down  State/Pfx

	10.10.10.10     65551             647      397      73711    0   (0) 10:37:12         5
	10.10.100.1     65552             664      416      73711    0   (0) 10:38:27         0
	10.100.10.9     65553             709      526      73711    0   (0) 07:55:38         1

So, how to configure this?
    
**How to configure a record per line?**
    
    <delim per-line="true" />

Here the *delim* has no value, and **per-line="true"**
        
**How to skip the header?** 

The header in this case is everything before the row with the header columns, so here we tell it to run up to this line.
    
	<header-delim><![CDATA[Neighbor\s+AS\s+MsgRcvd]]></header-delim>	

**How to include values in the header in every record?**
	
    <searches>
        <search id="numbers"><![CDATA[(\d+)]]></search>
        <search id="string"><![CDATA[(\S+?)]]></search>
    </searches>    
    <header>
        <value id="routerId"><![CDATA[BGP router identifier {string},]]></value>
        <value id="localAS"><![CDATA[BGP router identifier \S*?, local AS number {numbers}]]></value>
    </header>	

Here we use the *header* section to pull values out of all of the text in the header (i.e. all of the text **before** the **header-delim**.

The above configuration will include the values of *routerId* and *localAS* in all generated records.

**How do I pull multiple values out of the same line?**

    <searches>
        <search id="QUERY-LINE"><![CDATA[{ipAddress}\s+{numbers}(\s+\S+){5}\s+({numbersThenText})\s+{status}]]></search>
        <search id="numbers"><![CDATA[(\d+)]]></search>
        <search id="numbersThenText"><![CDATA[(\d+\S+)]]></search>
        <search id="string"><![CDATA[(\S+?)]]></search>
        <search id="ipAddress"><![CDATA[(\d+(\.\d+){3})]]></search>
        <search id="status"><![CDATA[((\d+)|(\D.*))]]></search>
    </searches>
    <values>
        <!-- here we reuse the line pattern, only we pull out different group values -->
        <value id="remoteIp" group="1"><![CDATA[{QUERY-LINE}]]></value>
        <value id="remoteAS" group="3"><![CDATA[{QUERY-LINE}]]></value>
        <value id="uptime" group="5"><![CDATA[{QUERY-LINE}]]></value>
        <value id="status" group="7"><![CDATA[{QUERY-LINE}]]></value>
    </value>

Here, we define a search, *QUERY-LINE*, from a bunch of other searches. 
 
**NOTE** if a search is to use another search (as *QUERY-LINE* uses *numbers*, then *numbers* **MUST** be defined **AFTER** *QUERY-LINE*

This *QUERY-LINE* has multiple groups, so the value that uses this search can select the group of interest.

So the values of *remoteIp* comes from the first group, and the value of *uptime* comes from the fifth group.
	
## Sample file with multiple delimiters

Suppose you have a file where there's a header with some values, and then records that come in different formats, e.g. a juniper BGP summary file

	Groups: 3 Peers: 3 Down peers: 0
	Table          Tot Paths  Act Paths Suppressed    History Damp State    Pending
	inet.0               947        310          0          0          0          0
	inet6.0              849        807          0          0          0          0
	Peer                     AS      InPkt     OutPkt    OutQ   Flaps Last Up/Dwn State|#Active/Received/Damped...
	10.247.68.182         65550     131725   28179233       0      11     6w3d17h Establ
	  inet.0: 4/5/1
	  inet6.0: 0/0/0
	10.254.166.246        65550     136159   29104942       0       0      6w5d6h Establ
	  inet.0: 0/0/0
	  inet6.0: 7/8/1
	192.0.2.100           65551    1269381    1363320       0       1      9w5d6h 1/2/3 4/5/6
	
In this case:

1. We have three records, one for each peer
1. There is a some text before the records that we don't care about
1. The records are delimited in one of two ways:
    1. Either the **inet** values are set on multiple lines after the initial peer line
    1. *Or* they are contained in the initial peer line, at the end (instead of having **Establ** there) 

So, how to configure this?
    

**How to have two different delimiters?** 

This is just a case of defining multiple delimiters. They are checked in order, one after the other, until one matches the line. 

The first matching delimiter is the one that is used.

    <delim retain-delim="true">{ipAddress}.*(\/\d+)\s*{EOL}</delim>
    <delim>\s*({inet6})</delim>    


The **retain-delim="true"** tells the parser that it is to include the delimiter line as part of the *next* record we parse. 

We do it here so that when we're processing one record, we will stop when get to a line with a peer ip address at the start, 
but since this line is the beginning of the next record, we want to include it in that record.


**How do I pull a value from multiple places**

In the following file, the value of, for example, *active inet v6* can come from two places.

	10.254.166.246        65550     136159   29104942       0       0      6w5d6h Establ
	  inet.0: 0/0/0
	  inet6.0: 7/8/1
	192.0.2.100           65551    1269381    1363320       0       1      9w5d6h 1/2/3 4/5/6

For peer *10.254.166.246* it's taken from the first value on the *inet6.0:* line, while for *192.0.2.100* it comes from the end peer line. 

We solve this by defining multiple values. The parser will apply them all, and take the value from the last entry that can derive a non-null value.

    <searches>
        <search id="QUERY-LINE"><![CDATA[\s*{ipAddress}\s+{numbers}\s+{numbers}\s+{numbers}\s+{numbers}\s+{numbers}\s+{numbersThenText}]]></search>
        <search id="inet"><![CDATA[{numbers}/{numbers}/{numbers}]]></search>
                
        <search id="numbers"><![CDATA[(\d+)]]></search>
        <search id="numbersThenText"><![CDATA[(\d+\S+)]]></search>
        <search id="ipAddress"><![CDATA[(\d+(\.\d+){3})]]></search>                        
    </searches>
    <values>
        <value id="activeV6" group="1"><![CDATA[inet6.0:\s*{inet}]]></value>
        <value id="activeV6" group="12"><![CDATA[{QUERY-LINE}\s*{inet} {inet}]]></value>
    </value>
    
So here, we have the following *searches* of interest 

* **inet**, that matches against three sets of digits (e.g 3/5/7)
* **QUERY-LINE**, that matches against *peer* line (without the ending, so without *Establ* or the inet values

And then we have to *values* for **activeV6**

One that matches against the line with inet6.0, and so would match this record

    10.254.166.246        65550     136159   29104942       0       0      6w5d6h Establ
      inet.0: 0/0/0
      inet6.0: 7/8/1
 	  
One that matches against the peer line with two *inet* values at the end, and so would match this record

	192.0.2.100           65551    1269381    1363320       0       1      9w5d6h 1/2/3 4/5/6

## Sample file where the delimiter is at the START of the record
    
By default the parser assumes the delimiter is at the end of a record, in some cases, it will be at the beginning. For example, an ifcfg output looks like this:

	lo0: flags=8049<UP,LOOPBACK,RUNNING,MULTICAST> mtu 16384
		inet6 ::1 prefixlen 128
		inet6 fe80::1%lo0 prefixlen 64 scopeid 0x1
		inet 127.0.0.1 netmask 0xff000000
	en0: flags=8863<UP,BROADCAST,SMART,RUNNING,SIMPLEX,MULTICAST> mtu 1500
		ether 34:15:9e:27:45:e3
		inet6 fe80::3615:9eff:fe27:45e3%en0 prefixlen 64 scopeid 0x4
		inet6 2001:db8::3615:9eff:fe27:45e3 prefixlen 64 autoconf
		inet 192.0.2.215 netmask 0xfffffe00 broadcast 192.0.2.255
		media: autoselect (1000baseT <full-duplex,flow-control>)
		status: active
	en1: flags=8863<UP,BROADCAST,SMART,RUNNING,SIMPLEX,MULTICAST> mtu 1500
		ether 90:84:0d:f6:d1:55
		media: <unknown subtype> (<unknown type>)
		status: inactive
		
Now, in this case, we have a set of three *interfaces*, but the delimiter is at the start of the record.

    <searches>
        <search id="interface"><![CDATA[(\S{2}\d):]]></search>
    </searches>
    <delim delim-at-start="true">^{interface}.*</delim>
    
What this **delim-at-start="true"** does is tell the parser that the record runs from this entry up to the next delimiter.

When *delim-at-start* is used, then the delimiter is included in the record.

# Loading a file

The code for doing so is this:

	import com.sonalake.utah.Parser;
	import com.sonalake.utah.parser.config.Config;
	import com.sonalake.utah.parser.config.ConfigLoader;
    
	
	// load a config, using a URL or a Reader
	Config config = new ConfigLoader().loadConfig(configURL);

	// load a file and iterate through the records
    List<Map<String, String>> observedValues = new ArrayList<Map<String, String>>();
    try (Reader in = new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream(fileResource))) {
      Parser parser = Parser.parse(config, in);
      while (true) {
        Map<String, String> record = parser.next();
        if (null == record) {
          break;
        } else {
          observedValues.add(record);
        }
      }
    }

# Building the application

    mvn clean install

This will run the unit tests, which are broken down into two parts:

- com.sonalake.utah.config.* - these are the basic unit tests for the class
- com.sonalake.utah.ExamplesTest - this processes the sample example files and template configurations that can be found in **utah-parser/src/test/resources/examples**


# Why Utah?

It stands for "unstructured text analytical handler" and has nothing to do with [Johnny Utah](http://www.imdb.com/title/tt0102685/)... honest...
