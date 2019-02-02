package com.axway.apim;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.security.cert.CertificateException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.UnrecognizedOptionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

	private static Logger logger = LoggerFactory.getLogger(Main.class);

	public Main() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) throws ParseException {
		// args = new String[] { "--operation=deploy",
		// "--gatewayURL=https://localhost:8090", "--username=admin",
		// "--password=changeme", "--group=lambda",
		// "--fedFile=D:\\api\\lambda.fed",
		// "--polFile=D:\\api\\lambda.pol", "--type=fed" };

		APIManagerWrapper apiManagerWrapper = new APIManagerWrapper();

		Options options = options();

		if (checkForHelp(args, options)) {
			usage();
			System.exit(1);
		}

		CommandLineParser parser = new DefaultParser();

		CommandLine line = null;

		try {
			line = parser.parse(options, args);
		} catch (UnrecognizedOptionException e) {
			logger.error(e.getMessage());
			usage();
			System.exit(1);
		}

		String operation = line.getOptionValue("operation");
		String url = line.getOptionValue("url");
		String username = line.getOptionValue("username");
		String password = line.getOptionValue("password");
		String artifactLocation = line.getOptionValue("artifactlocation");

		String orgName = null;
		String backendAuthJson = null;
		String backendURL = null;
		String outboundCertDir = null;
		String apiName = null;
		String apiVersion = null;

		if (operation.equalsIgnoreCase("export")) {
			if (line.hasOption('n')) {
				apiName = line.getOptionValue("n");
			} else {
				logger.error("Provide API Name");
				System.exit(1);
			}

			if (line.hasOption('v')) {
				apiVersion = line.getOptionValue("v");
			} else {
				logger.error("Provide API Version");
				System.exit(1);
			}
			List<String> version = new ArrayList<>();
			version.add(apiVersion);
			logger.info("Exportig API {} with Version {}", apiName, apiVersion);
			try {
				apiManagerWrapper.exportAPIs(url, username, password, artifactLocation, apiName, version);
			} catch (IOException e) {
				logger.error("Export failed : {}", e);
			}

			logger.info("API Exported to location {}", artifactLocation);

		} else if (operation.equals("deploy")) {
			
			if (line.hasOption('d')) {
				orgName = line.getOptionValue("d");
			} else {
				logger.error("Provide Developer Organization Name");
				System.exit(1);
			}
			
			if (line.hasOption("bu")) {
				backendURL = line.getOptionValue("bu");
			}

			if (line.hasOption("ba")) {
				backendAuthJson = line.getOptionValue("ba");
			}
			if (line.hasOption("oc")) {
				outboundCertDir = line.getOptionValue("oc");
			}
			
			try {
				apiManagerWrapper.importAPIs(url, username, password, artifactLocation, orgName, backendURL, outboundCertDir, backendAuthJson);
			} catch (IOException | CertificateException e) {
				logger.error("Deployment failed : {}", e);
			}

		} else {
			logger.error("Provide proper type: fed or polenv");
			System.exit(1);
		}

	}

	private static boolean checkForHelp(String[] args, Options options) throws ParseException {

		if (args.length == 0)
			return true;

		for (String argument : args) {
			if (argument.equals("-h") || argument.equals("--help")) {
				return true;
			}
		}
		return false;
	}

	private static Options options() {
		Options options = new Options();
		Option help = Option.builder("h").longOpt("help").required(false).hasArg(false).desc("Help options").build();

		Option operation = Option.builder("o").longOpt("operation").required(true).hasArg(true)
				.desc("Name of Operation : export  or deploy ").build();

		Option url = Option.builder("s").longOpt("url").required(true).hasArg(true).desc("API Manager URL").build();

		Option username = Option.builder("u").longOpt("username").required(true).hasArg(true)
				.desc("API  Manager Username").build();

		Option password = Option.builder("p").longOpt("password").required(true).hasArg(true)
				.desc("API Manager password").build();

		Option artifactLocation = Option.builder("a").longOpt("artifactlocation").required(true).hasArg(true)
				.desc("Artifact Location (Directory)").build();

		Option apiVersion = Option.builder("v").longOpt("version").required(false).hasArg(true).desc("API Version")
				.build();

		Option apiNAme = Option.builder("n").longOpt("apiname").required(false).hasArg(true).desc("Name of the API")
				.build();
		
		Option devOrgName = Option.builder("d").longOpt("orgname").required(false).hasArg(true).desc("Developer Organization Name")
				.build();

		Option backendAuth = Option.builder("ba").longOpt("backendauth").required(false).hasArg(true)
				.desc("Backend Authentication JSON").build();
		Option backendURL = Option.builder("bu").longOpt("backendurl").required(false).hasArg(true).desc("Backend URL")
				.build();

		Option outboundCert = Option.builder("oc").longOpt("outboundcert").required(false).hasArg(true)
				.desc("Outbound Certficate directory").build();

		options.addOption(help);
		options.addOption(operation);
		options.addOption(url);
		options.addOption(username);
		options.addOption(password);
		options.addOption(apiVersion);
		options.addOption(apiNAme);
		options.addOption(artifactLocation);
		options.addOption(devOrgName);
		options.addOption(backendURL);
		options.addOption(backendAuth);
		options.addOption(outboundCert);
		return options;
	}

	private static void usage() {
		new HelpFormatter().printHelp("API  Deployment", options());
	}

}
