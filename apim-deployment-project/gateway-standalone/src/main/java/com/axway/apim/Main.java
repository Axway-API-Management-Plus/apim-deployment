package com.axway.apim;

import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {

        Options options = options();
        if (checkForHelp(args)) {
            usage();
            System.exit(1);
        }
        CommandLineParser parser = new DefaultParser();
        CommandLine line = null;
        try {
            line = parser.parse(options, args);
        } catch (ParseException e) {
            LOGGER.error(e.getMessage());
            usage();
            System.exit(1);
        }

        String operation = line.getOptionValue("operation");
        String type = line.getOptionValue("type");

        String url = line.getOptionValue("s");
        String username = line.getOptionValue("username");
        String password = line.getOptionValue("password");
        String groupName = line.getOptionValue("group");
        boolean inSecure = line.hasOption("insecure");

        String fedFileName = null;
        String fedDir = null;
        String polFileName = null;
        String envFileName = null;
        String instanceName = null;

        if (line.hasOption('n')) {
            instanceName = line.getOptionValue("n");
        }

        if (type.equalsIgnoreCase("fed")) {
            if (line.hasOption('f')) {
                fedFileName = line.getOptionValue("f");
            } else if(line.hasOption('d')){
                fedDir = line.getOptionValue("d");
            }else {
                LOGGER.error("Provide fed location");
                System.exit(1);
            }
        } else if (type.equals("polenv")) {
            if (line.hasOption("pol")) {
                polFileName = line.getOptionValue("pol");
            } else {
                LOGGER.error("Provide policy location");
                System.exit(1);
            }

            if (line.hasOption('e')) {
                envFileName = line.getOptionValue("e");
            } else {
                LOGGER.error("Provide enviroment location");
                System.exit(1);
            }

        } else {
            LOGGER.error("Provide proper type: fed or polenv");
            System.exit(1);
        }

        AxwayClient axwayClient = AxwayClient.getInstance();
        GatewayDeployment gatewayDeployment = new GatewayDeployment(axwayClient);
        Orchestrator orchestrator = new Orchestrator(gatewayDeployment);
        if (operation.equalsIgnoreCase("export")) {
            orchestrator.download(url, username, password, groupName, instanceName, type, fedFileName, polFileName,
                envFileName, inSecure);
        } else if (operation.equalsIgnoreCase("deploy")) {
            orchestrator.deploy(url, username, password, groupName, instanceName, type, fedFileName, polFileName,
                envFileName, inSecure, fedDir);
        } else {
            LOGGER.error("Provide valid operation name: possible values download or deploy");
            System.exit(1);
        }

    }

    private static boolean checkForHelp(String[] args) {

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

        Option url = Option.builder("s").longOpt("gatewayURL").required(true).hasArg(true)
            .desc("Admin Node Manager URL").build();

        Option username = Option.builder("u").longOpt("username").required(true).hasArg(true)
            .desc("Admin Node Manager username").build();

        Option password = Option.builder("p").longOpt("password").required(true).hasArg(true)
            .desc("Admin Node Manager password").build();

        Option groupName = Option.builder("g").longOpt("group").required(true).hasArg(true).desc("Domain group name")
            .build();

        Option instanceName = Option.builder("n").longOpt("instance").required(false).hasArg(true)
            .desc("Domain instance name").build();

        Option fedFile = Option.builder("f").longOpt("fedFile").required(false).hasArg(true).desc("Federation File")
            .build();

        Option fedDir = Option.builder("d").longOpt("fedDir").required(false).hasArg(true).desc("Federation directory")
            .build();

        Option polFile = Option.builder("pol").longOpt("polFile").required(false).hasArg(true).desc("PolicyFile File")
            .build();

        Option envFile = Option.builder("e").longOpt("envFile").required(false).hasArg(true).desc("Environment File")
            .build();

        Option type = Option.builder("t").longOpt("type").required(false).hasArg(true)
            .desc("Possible values: fed, polenv").build();

        Option inSecure = Option.builder().longOpt("insecure").required(false).hasArg(false).desc("disable certificate and hostname verification").build();

        options.addOption(operation);
        options.addOption(url);
        options.addOption(username);
        options.addOption(password);
        options.addOption(groupName);
        options.addOption(instanceName);

        options.addOption(fedFile);
        options.addOption(polFile);
        options.addOption(envFile);
        options.addOption(help);
        options.addOption(type);
        options.addOption(inSecure);
        options.addOption(fedDir);
        return options;
    }

    private static void usage() {
        new HelpFormatter().printHelp("Gateway Deployment", options());
    }

}
