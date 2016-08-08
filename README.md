# Utah Parser [![Build Status](https://travis-ci.org/sonalake/utah-parser.png?branch=master)](https://travis-ci.org/sonalake/utah-parser) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.sonalake/utah-parser/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.sonalake/utah-parser) [![Coverage Status](https://coveralls.io/repos/github/sonalake/utah-parser/badge.svg?branch=master)](https://coveralls.io/github/sonalake/utah-parser?branch=master) [![License](http://img.shields.io/:license-apache-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)
**A Java library for parsing semi-structured text files**

The purpose of this library is to allow semi-structured text files to be parsed without having to resort to hand-crafting a parser in Java. Text files that meet the following criteria are supported:

1. The file is to be parsed into a list of records, where each record is a simple name-value pair 
(so, none of the fields are lists or anything complex).
1. Every record is delimited by either one of set of *single-line* regular expressions, or the end of the file.
1. Every value in the record can be identified by a *multiple-line* regular expression. While the regex may have multiple groups the `value` comes from a single group. The `group` attribute identifies the group to populate the `value` with.

What we were looking for was something that could handle what [TextFSM](https://github.com/google/textfsm) could do, but in Java. 

## Configuration

The parser is configured using an XML 'template' file.

```xml
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
```    	

The `delim` element identifies the boundary between records in an input file. The regular expressions defined in
the `values` section are used to extract values from the input file. The optional `searches` section
allows you to define a regular expression once and then reuse it in multiple values.

**NOTE:** the delimiters are applied per line. That is, the file is processed a line at a time, and if a line matches 
the regular expression then the text before that line is parsed for record values. All of the processed text is then ignored
on the next iteration. If the file finishes before a delimiter is found then all the remaining text is treated as a record.
For example, the `numbers` search above is used within two `values` above.
     
 The curly-braces are used to do the substitution and multiple substitutions can be used. A *search* can even contain 
 other searches, however, they are applied **once** and in the order in which they appear in the config file.

 Each regex is applied to the entire record and the first match is used.  

## Example 1: Simple delimiter

Assuming the template above, the following file would result in two records.

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

Then two extracted records are:

**NOTE:** the application doesn't return a json doc, rather it returns an array of maps, this example is illustrative.

```json
[
  {
    "numberField" : "123", 
    "someOtherNumberField" : "456", 
    "stringFieldA" : "what'll I do?", 
    "stringFieldB" : "without you?", 
    "startOfInterval" : "10/22/2015 12:00:00 AM",
    "endOfInterval" : "12/31/2015 11:59:00 PM",
  },
  {
    "numberField" : "987", 
    "someOtherNumberField" : "654", 
    "stringFieldA" : "some fields are missing!", 
    "startOfInterval" : "10/22/2015 12:00:00 AM"
  }
]
```
 
The records are a simple `List<Map<String, String>>` and fields with no value will not be included.

## Example 2: Record per line, header values in each record

	BGP router identifier 192.0.2.1, local AS number 65551

	Neighbor        AS            MsgRcvd  MsgSent     TblVer  InQ  OutQ Up/Down  State/Pfx

	10.10.10.10     65551             647      397      73711    0   (0) 10:37:12         5
	10.10.100.1     65552             664      416      73711    0   (0) 10:38:27         0
	10.100.10.9     65553             709      526      73711    0   (0) 07:55:38         1

Where we want a result file like this:

```json
[
  {
    "localAS": "65551",
    "remoteAS": "65551",
    "remoteIp": "10.10.10.10",
    "routerId": "192.0.2.1",
    "status": "5",
    "uptime": "10:37:12"
  },
  {
    "localAS": "65551",
    "remoteAS": "65552",
    "remoteIp": "10.10.100.1",
    "routerId": "192.0.2.1",
    "status": "0",
    "uptime": "10:38:27"
  },
  {
    "localAS": "65551",
    "remoteAS": "65553",
    "remoteIp": "10.100.10.9",
    "routerId": "192.0.2.1",
    "status": "1",
    "uptime": "07:55:38"
  }
]
```

### Configure a record per line
```xml
<delim per-line="true" />
```
### Skip the header
        
The header in this case is everything before the row with the column names, so here we tell it to run up to this line.
```xml 
<header-delim><![CDATA[Neighbor\s+AS\s+MsgRcvd]]></header-delim>	
```
### Include header values in each record

```xml	
<searches>
    <search id="numbers"><![CDATA[(\d+)]]></search>
    <search id="string"><![CDATA[(\S+?)]]></search>
</searches>    
<header>
    <value id="routerId"><![CDATA[BGP router identifier {string},]]></value>
    <value id="localAS"><![CDATA[BGP router identifier \S*?, local AS number {numbers}]]></value>
</header>
```

Here we use the `header` section to pull values out of all of the text in the header (i.e. all of the text before the `header-delim`.

The above configuration will include the values of *routerId* and *localAS* in all generated records.

### Extract multiple values from a single line

```xml
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
```

Here, we define the `QUERY-LINE` search from the `numbersThenText` and `status` searches.
 
**NOTE** if a search needs to use another search then it must be define before those other searches.

This `QUERY-LINE` search has multiple groups, so the value that uses this search can select the `group` attribute. Groups are offset from 1, so the values of `remoteIp` comes from the first group, and the value of `uptime` comes from the fifth group.
	
	
## Example 3: File with multiple delimiters

Suppose you have a file where there's a header with some values, followed by records that that can appear in different formats. For example a Juniper BGP summary file:

```
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
```

Where we want a result file like this:

```json
[
  {
    "accepted_V4": "1",
    "accepted_V6": "0",
    "activeV4": "4",
    "activeV6": "0",
    "receivedV4": "5",
    "receivedV6": "0",
    "remoteIp": "10.247.68.182",
    "uptime": "6w3d17h"
  },
  {
    "accepted_V4": "0",
    "accepted_V6": "1",
    "activeV4": "0",
    "activeV6": "7",
    "receivedV4": "0",
    "receivedV6": "8",
    "remoteIp": "10.254.166.246",
    "uptime": "6w5d6h"
  },
  {
    "accepted_V4": "3",
    "accepted_V6": "6",
    "activeV4": "1",
    "activeV6": "4",
    "receivedV4": "2",
    "receivedV6": "5",
    "remoteIp": "192.0.2.100",
    "uptime": "9w5d6h"
  }
]
```

In this case:
* There are three records, one for each peer (`10.247.68.182`, `10.254.166.246`, `192.0.2.100`)
* There is a some text we can ignore before the records (first five lines of the file)
* The records appear in two forms:
    1. `inet` values appear on multiple lines after the peer line
    1. `inet` values appear at the end of the peer line

To parse handle such a scenario we need to configure two different delimiters.
    

### Multiple delimiters

When multiple delimiters are declared they are checked in order until one matches the line. The first matching delimiter is the one that is used.
```xml
<delim retain="true">{ipAddress}.*(\/\d+)\s*{EOL}</delim>
<delim>\s*({inet6})</delim>    
```

The `retain="true"` tells the parser that it is to include the delimiter line as part of the *next* record we parse. 

We do it here so that when we're processing one record, we will stop when get to a line with a peer ip address at the start, 
but since this line is the beginning of the next record, we want to include it in that record.


**How do I pull a value from multiple places**

In the following file, the value of, for example, `activeV6` can come from two places.

	10.254.166.246        65550     136159   29104942       0       0      6w5d6h Establ
	  inet.0: 0/0/0
	  inet6.0: 7/8/1
	192.0.2.100           65551    1269381    1363320       0       1      9w5d6h 1/2/3 4/5/6

For peer `10.254.166.246` it's taken from the first value on the *inet6.0:* line, while for `192.0.2.100` it comes from the end peer line. 

We solve this by defining multiple values. The parser will apply them all, and take the value from the last entry that can derive a non-null value.

```xml
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
```

So here, we have the following *searches* of interest 

* `inet`, that matches against three sets of digits (e.g 3/5/7)
* `QUERY-LINE`, that matches against *peer* line (without the ending, so without *Establ* or the inet values

And then we have two *values* for `activeV6`

One that matches against the line with inet6.0, and so would match this record

    10.254.166.246        65550     136159   29104942       0       0      6w5d6h Establ
      inet.0: 0/0/0
      inet6.0: 7/8/1
 	  
One that matches against the peer line with two *inet* values at the end, and so would match this record

	192.0.2.100           65551    1269381    1363320       0       1      9w5d6h 1/2/3 4/5/6

## Example 4: File with delimiter at the start of each record
    
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
		
Where we want a result file like this:
```json
[
  {
    "inet4": "127.0.0.1",
    "inet6": "::1",
    "interface": "lo0",
    "mtu": "16384",
    "netmask": "0xff000000",
    "prefixlen": "128"
  },
  {
    "ether": "34:15:9e:27:45:e3",
    "inet4": "192.0.2.215",
    "inet6": "2001:db8::3615:9eff:fe27:45e3",
    "interface": "en0",
    "mtu": "1500",
    "netmask": "0xfffffe00",
    "prefixlen": "64"
  },
  {
    "ether": "90:84:0d:f6:d1:55",
    "interface": "en1",
    "mtu": "1500"
  }
]
```

		
Now, in this case, we have a set of three *interfaces*, but the delimiter is at the start of the record.

```xml
<searches>
    <search id="interface"><![CDATA[(\S{2}\d):]]></search>
</searches>
<delim at-start="true">^{interface}.*</delim>
```
    
What `at-start="true"` does is tell the parser that the record runs from this entry up to the next delimiter.

# Loading a file

```java
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
```    

# Building the application

    mvn clean install

This will run the unit tests, which are broken down into two parts:

- `com.sonalake.utah.config.*` - these are the basic unit tests for the class
- `com.sonalake.utah.ExamplesTest` - this processes the sample example files and template configurations that can be found in `utah-parser/src/test/resources/examples`. 
These are the same files that are included in the root examples directory. 

## GPG signing

The `install` step performs a GPG signing of the jars, if you don't have any keys for this, for local development,
you can skip this step by using

    mvn clean install -DskipGpgSign=true

# License
Code is under the [Apache Licence v2](https://www.apache.org/licenses/LICENSE-2.0.txt).

# Why Utah?

It stands for "unstructured text analytical handler" and has nothing to do with [Johnny Utah](http://www.imdb.com/title/tt0102685/), honest.
