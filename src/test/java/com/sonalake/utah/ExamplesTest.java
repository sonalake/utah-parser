package com.sonalake.utah;

import com.sonalake.utah.config.Config;
import com.sonalake.utah.config.ConfigLoader;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * A test to load up a file and confirm it parses ok
 */
public class ExamplesTest {

  /**
   * In this test we load up the sample config and then process the sample input file, and then confirm the
   * data records are as expected.
   *
   * @throws IOException
   * 
   */
  @Test
  public void testSampleFile() throws IOException, IOException {
    String configResource = "sample.config.xml";
    String fileResource = "sample.import.txt";

    // these are the files we expect from the config
    List<Map<String, String>> expectedResults = new ArrayList<Map<String, String>>() {{
      add(new TreeMap<String, String>() {{
        put("numberField", "123");
        put("someOtherNumberField", "456");
        put("stringFieldA", "what'll I do?");
        put("stringFieldB", "without you?");
        put("startOfInterval", "10/22/2015 12:00:00 AM");
        put("endOfInterval", "12/31/2015 11:59:00 PM");
      }});
      add(new TreeMap<String, String>() {{
        put("numberField", "987");
        put("someOtherNumberField", "654");
        put("stringFieldA", "some fields are missing!");
        put("startOfInterval", "10/22/2015 12:00:00 AM");
      }});
    }};
    testFileProcessing(configResource, fileResource, expectedResults);

  }

  /**
   * In this test we load up the sample config and then process the sample input file, and then confirm the
   * data records are as expected.
   *
   * @throws IOException
   * 
   */
  @Test
  public void testExampleCiscoVersion() throws IOException, IOException {
    String configResource = "examples/cisco_version_template.xml";
    String fileResource = "examples/cisco_version_example.txt";

    // these are the files we expect from the config
    List<Map<String, String>> expectedResults = new ArrayList<Map<String, String>>() {{
      add(new TreeMap<String, String>() {{
        put("version", "12.2(31)SGA1");
        put("uptime", "3 days, 13 hours, 53 minutes");
        put("reloadReason", "reload");
        put("reloadTime", "05:09:09 PDT Wed Apr 2 2008");
        put("imageFile", "bootflash:cat4500-entservicesk9-mz.122-31.SGA1.bin");
        put("model", "WS-C4948-10GE");
        put("memory", "262144K");
        put("configRegister", "x2102");
      }});
    }};
    testFileProcessing(configResource, fileResource, expectedResults);

  }


  /**
   * In this test we load up the sample config and then process the sample input file, and then confirm the
   * data records are as expected.
   *
   * @throws IOException
   * 
   */
  @Test
  public void testExampleCiscoBgpSurvey() throws IOException, IOException {
    String configResource = "examples/cisco_bgp_summary_template.xml";
    String fileResource = "examples/cisco_bgp_summary_example.txt";

    // these are the files we expect from the config
    List<Map<String, String>> expectedResults = new ArrayList<Map<String, String>>() {{

      //{localAS=65550, remoteAS=65551, remoteIp=192.0.2.77, routerId=192.0.2.70, status=1, uptime=5w4d},
      add(new TreeMap<String, String>() {{
        put("localAS", "65550");
        put("remoteAS", "65551");
        put("remoteIp", "192.0.2.77");
        put("routerId", "192.0.2.70");
        put("status", "1");
        put("uptime", "5w4d");
      }});
      //{localAS=65550, remoteAS=65552, remoteIp=192.0.2.78, routerId=192.0.2.70, status=10, uptime=5w4d}

      add(new TreeMap<String, String>() {{
        put("localAS", "65550");
        put("remoteAS", "65552");
        put("remoteIp", "192.0.2.78");
        put("routerId", "192.0.2.70");
        put("status", "10");
        put("uptime", "5w4d");
      }});
    }};
    testFileProcessing(configResource, fileResource, expectedResults);
  }

  /**
   * In this test we load up the sample config and then process the sample input file, and then confirm the
   * data records are as expected.
   *
   * @throws IOException
   * 
   */
  @Test
  public void testExampleF10IPBgpSurvey() throws IOException, IOException {
    String configResource = "examples/f10_ip_bgp_summary_template.xml";
    String fileResource = "examples/f10_ip_bgp_summary_example.txt";

    // these are the files we expect from the config
    List<Map<String, String>> expectedResults = new ArrayList<Map<String, String>>() {{

      add(new TreeMap<String, String>() {{
        put("localAS", "65551");
        put("remoteAS", "65551");
        put("remoteIp", "10.10.10.10");
        put("routerId", "192.0.2.1");
        put("status", "5");
        put("uptime", "10:37:12");
      }});
      add(new TreeMap<String, String>() {{
        put("localAS", "65551");
        put("remoteAS", "65552");
        put("remoteIp", "10.10.100.1");
        put("routerId", "192.0.2.1");
        put("status", "0");
        put("uptime", "10:38:27");
      }});
      add(new TreeMap<String, String>() {{
        put("localAS", "65551");
        put("remoteAS", "65553");
        put("remoteIp", "10.100.10.9");
        put("routerId", "192.0.2.1");
        put("status", "1");
        put("uptime", "07:55:38");
      }});
    }};
    testFileProcessing(configResource, fileResource, expectedResults);
  }


  /**
   * In this test we load up the sample config and then process the sample input file, and then confirm the
   * data records are as expected.
   *
   * @throws IOException
   * 
   */
  @Test
  public void testExampleF10Version() throws IOException, IOException {
    String configResource = "examples/f10_version_template.xml";
    String fileResource = "examples/f10_version_example.txt";

    // these are the files we expect from the config
    List<Map<String, String>> expectedResults = new ArrayList<Map<String, String>>() {{
      add(new TreeMap<String, String>() {{
        put("software", "7.7.1.1");
        put("chassis", "E1200");
        put("model", "E1200");
        put("imageFile", "flash://FTOS-EF-7.7.1.1.bin");
      }});
    }};
    testFileProcessing(configResource, fileResource, expectedResults);

  }


  /**
   * In this test we load up the sample config and then process the sample input file, and then confirm the
   * data records are as expected.
   *
   * @throws IOException
   * 
   */
  @Test
  public void testJuniperBgpVersion() throws IOException, IOException {
    String configResource = "examples/juniper_bgp_summary_template.xml";
    String fileResource = "examples/juniper_bgp_summary_example.txt";

    // these are the files we expect from the config
    List<Map<String, String>> expectedResults = new ArrayList<Map<String, String>>() {{
      /*
      10.247.68.182         65550     131725   28179233       0      11     6w3d17h Establ
        inet.0: 4/5/1
        inet6.0: 0/0/0
       */
      add(new TreeMap<String, String>() {{
        put("remoteIp", "10.247.68.182");
        put("uptime", "6w3d17h");
        put("activeV4", "4");
        put("receivedV4", "5");
        put("accepted_V4", "1");
        put("activeV6", "0");
        put("receivedV6", "0");
        put("accepted_V6", "0");
      }});
      /*
      10.254.166.246        65550     136159   29104942       0       0      6w5d6h Establ
        inet.0: 0/0/0
        inet6.0: 7/8/1
       */
      add(new TreeMap<String, String>() {{
        put("remoteIp", "10.254.166.246");
        put("uptime", "6w5d6h");
        put("activeV4", "0");
        put("receivedV4", "0");
        put("accepted_V4", "0");
        put("activeV6", "7");
        put("receivedV6", "8");
        put("accepted_V6", "1");
      }});

      /*
        192.0.2.100           65551    1269381    1363320       0       1      9w5d6h 1/2/3 4/5/6
       */
      add(new TreeMap<String, String>() {{
        put("remoteIp", "192.0.2.100");
        put("uptime", "9w5d6h");
        put("activeV4", "1");
        put("receivedV4", "2");
        put("accepted_V4", "3");
        put("activeV6", "4");
        put("receivedV6", "5");
        put("accepted_V6", "6");
      }});
    }};
    testFileProcessing(configResource, fileResource, expectedResults);

  }


  /**
   * In this test we load up the sample config and then process the sample input file, and then confirm the
   * data records are as expected.
   *
   * @throws IOException
   * 
   */
  @Test
  public void testExampleJuniperVersion() throws IOException, IOException {
    String configResource = "examples/juniper_version_template.xml";
    String fileResource = "examples/juniper_version_example.txt";

    // these are the files we expect from the config
    List<Map<String, String>> expectedResults = new ArrayList<Map<String, String>>() {{
      add(new TreeMap<String, String>() {{
        put("model", "mx960");
        put("junosOsBoot", "9.1S3.5");
        put("junosOsSoftware", "9.1S3.5");
        put("junosKernelSoftware", "9.1S3.5");
        put("junosCryptoSoftware", "9.1S3.5");
        put("junosPacketForwardMTCommon", "9.1S3.5");
        put("junosPacketForwardMXCommon", "9.1S3.5");
        put("junosOnlineDoc", "9.1S3.5");
        put("junosRoutingSoftware", "9.1S3.5");
      }});
    }};
    testFileProcessing(configResource, fileResource, expectedResults);
  }


  /**
   * In this test we load up the sample config and then process the sample input file, and then confirm the
   * data records are as expected.
   *
   * @throws IOException
   * 
   */
  @Test
  public void testExampleIfcfg() throws IOException, IOException {
    String configResource = "examples/unix_ifcfg_template.xml";
    String fileResource = "examples/unix_ifcfg_example.txt";

    // these are the files we expect from the config
    List<Map<String, String>> expectedResults = new ArrayList<Map<String, String>>() {{
      /*
      lo0: flags=8049<UP,LOOPBACK,RUNNING,MULTICAST> mtu 16384
        inet6 ::1 prefixlen 128
        inet6 fe80::1%lo0 prefixlen 64 scopeid 0x1
        inet 127.0.0.1 netmask 0xff000000
       */
      add(new TreeMap<String, String>() {{
        put("interface", "lo0");
        put("mtu", "16384");
        put("inet6", "::1");
        put("prefixlen", "128");
        put("inet4", "127.0.0.1");
        put("netmask", "0xff000000");
      }});
      /*
        en0: flags=8863<UP,BROADCAST,SMART,RUNNING,SIMPLEX,MULTICAST> mtu 1500
          ether 34:15:9e:27:45:e3
          inet6 fe80::3615:9eff:fe27:45e3%en0 prefixlen 64 scopeid 0x4
          inet6 2001:db8::3615:9eff:fe27:45e3 prefixlen 64 autoconf
          inet 192.0.2.215 netmask 0xfffffe00 broadcast 192.0.2.255
          media: autoselect (1000baseT <full-duplex,flow-control>)
          status: active
       */
      add(new TreeMap<String, String>() {{
        put("interface", "en0");
        put("ether", "34:15:9e:27:45:e3");
        put("mtu", "1500");
        put("inet6", "2001:db8::3615:9eff:fe27:45e3");
        put("prefixlen", "64");
        put("inet4", "192.0.2.215");
        put("netmask", "0xfffffe00");
      }});

      /*
        en1: flags=8863<UP,BROADCAST,SMART,RUNNING,SIMPLEX,MULTICAST> mtu 1500
          ether 90:84:0d:f6:d1:55
          media: <unknown subtype> (<unknown type>)
          status: inactive
       */
      add(new TreeMap<String, String>() {{
        put("interface", "en1");
        put("ether", "90:84:0d:f6:d1:55");
        put("mtu", "1500");
      }});
    }};
    testFileProcessing(configResource, fileResource, expectedResults);
  }


  /**
   * Test file processing
   *
   * @param configResource  the resource name for a config
   * @param fileResource    the resource name for a file that is expected to match the config
   * @param expectedResults the json results expected from the processing
   * 
   * @throws IOException
   */
  private void testFileProcessing(String configResource, String fileResource, List<Map<String, String>>
    expectedResults) throws IOException, IOException {
    // load the config
    URL configURL = Thread.currentThread().getContextClassLoader().getResource(configResource);
    Config config = new ConfigLoader().loadConfig(configURL);

    // load a real file
    List<Map<String, String>> observedValues = new ArrayList<Map<String, String>>();
    try (Reader in = new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream
      (fileResource))) {
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

    Assert.assertEquals(expectedResults, observedValues);
  }

}
