import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;

import bftclientproxy.BFTClientControl;
import bftserverproxy.BFTServerControl;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import hybridproxy.Control;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import simpleproxy.ControlSimple;

/**
 * Main class - entry point
 * 
 * @author gardine1
 *
 */
public class ProxyMain {
	static final int LOCAL_PORT = 111;
	static final String REMOTE_HOST = "192.168.122.160";
	static final int REMOTE_PORT = 6653;

	public static void main(String[] args) {
		Logger root = (Logger)LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
		root.setLevel(Level.INFO);
		Properties prop = new Properties();
		InputStream input = null;

		try {

			input = new FileInputStream(args[0]);

			// load a properties file
			prop.load(input);

			// get the property value and print it out
			System.out.println(prop.getProperty("version"));
			String version = prop.getProperty("version");
			if (version.equalsIgnoreCase("simple")) { 
				System.out.println("Runnign simple proxy");
				ControlSimple c = new ControlSimple(prop); 
				c.runServer();

			} else if (version.equalsIgnoreCase("unsigned")) {
				System.out.println("Running unsigned proxy");
				Control c = new Control(prop);
				c.runServer();
 
			} else if (version.equalsIgnoreCase("signed")) {
				System.out.println("Running signed proxy");
//				ControlSigned c = new ControlSigned(prop);
//				c.runServer();
				signedv2.Control c = new signedv2.Control(prop);
				c.runServer();
			}else if (version.equalsIgnoreCase("signedv2")) {
				System.out.println("Running signed proxy");
				signedv2.Control c = new signedv2.Control(prop);
				c.runServer();

			}else if (version.equalsIgnoreCase("bftclient")) {
				System.out.println("Running bftclient proxy");
				BFTClientControl c = new BFTClientControl(prop);
				c.runServer();

			}else if (version.equalsIgnoreCase("bftserver")) {
				System.out.println("Running bftserver proxy");
				BFTServerControl c = new BFTServerControl(prop);
				c.runServer(Integer.parseInt(prop.getProperty("bftid")));

			}
			
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

}
