package es.tid.pce.client;


import es.tid.pce.pcep.constructs.Request;
import es.tid.pce.pcep.constructs.SVECConstruct;
import es.tid.pce.pcep.messages.PCEPMessage;
import es.tid.pce.pcep.messages.PCEPRequest;
import es.tid.pce.pcep.messages.PCEPResponse;
import es.tid.pce.pcep.objects.Svec;
import es.tid.pce.pcep.objects.UnknownObject;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;

public class QuickClientMain {


    public static final Logger Log = LoggerFactory.getLogger("PCCClient");

    public static void main(final String[] args) {


        Option gOpt = new Option("g", "Generalized end points");
        Option eroOpt = new Option("ero", "Explicit Route Object");
        Option iniOpt = new Option("ini", "Send init message");


        //Option rbwOpt= new Option("rbw", "Send rbw message");
        Option ofOpt = OptionBuilder.withArgName("value").hasArg().withDescription("set of value").create("of");
        Option rgbwOpt = OptionBuilder.withArgName("value").hasArg().withDescription("set rgbw value").create("rgbw");
        Option liOpt = OptionBuilder.withArgName("value").hasArg().withDescription("local interface").create("li");
        Option rbw1Opt = OptionBuilder.withArgName("value").hasArg().withDescription("set rbw1" +
                " value").create("rbw1");
        Option rbw2Opt = OptionBuilder.withArgName("value").hasArg().withDescription("set rbw2" +
                " value").create("rbw2");
        Option iro1Opt = OptionBuilder.withArgName("value").hasArg().withDescription("set of " +
                "iro1").create("iro1");
        Option iro2Opt = OptionBuilder.withArgName("value").hasArg().withDescription("set of " +
                "iro2").create("iro2");
        Option iron1Opt = OptionBuilder.withArgName("value").hasArg().withDescription("set of " +
                "iron1").create("iron1");
        Option iron2Opt = OptionBuilder.withArgName("value").hasArg().withDescription("set of " +
                "iron2").create("iron2");
        Option xro1Opt = OptionBuilder.withArgName("value").hasArg().withDescription("set of " +
                "xro1").create("xro1");
        Option xro2Opt = OptionBuilder.withArgName("value").hasArg().withDescription("set of " +
                "xro2").create("xro2");
        Option xron1Opt = OptionBuilder.withArgName("value").hasArg().withDescription("set of " +
                "xron1").create("xron1");
        Option xron2Opt = OptionBuilder.withArgName("value").hasArg().withDescription("set of " +
                "xron2").create("xron2");
        /*svec options*/
        Option ndivOpt = OptionBuilder.withArgName("value").hasArg().withDescription("set of " +
                "ndiv").create("ndiv");
        Option ldivOpt = OptionBuilder.withArgName("value").hasArg().withDescription("set of " +
                "ldiv").create("ldiv");
        Option srlgdivOpt = OptionBuilder.withArgName("value").hasArg().withDescription("set " +
                "of " + "srlgdiv").create("srlgdiv");
        Option adminGroupOpt = OptionBuilder.withArgName("value").hasArg().withDescription("set " +
                "of " + "adminGroup").create("adminGroup");


        Options options = new Options();
        options.addOption(liOpt);
        options.addOption(gOpt);
        options.addOption(eroOpt);
        options.addOption(iniOpt);
        options.addOption(ofOpt);
        options.addOption(rgbwOpt);
        options.addOption(rbw1Opt);
        options.addOption(rbw2Opt);
        options.addOption(iro1Opt);
        options.addOption(iro2Opt);
        options.addOption(iron1Opt);
        options.addOption(iron2Opt);
        options.addOption(xro1Opt);
        options.addOption(xro2Opt);
        options.addOption(xron1Opt);
        options.addOption(xron2Opt);
        options.addOption(ndivOpt);
        options.addOption(ldivOpt);
        options.addOption(srlgdivOpt);
        options.addOption(adminGroupOpt);


        if (args.length < 4) {
            //Log.info("Usage: ClientTester <host> <port> <src> <dst> [options]");
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("PCC-QuickClient <host> <port> <src> <dst> [options]", options);
            return;
        }

                    CommandLineParser parser = new DefaultParser();

                    try {
                        CommandLine line = parser.parse(options, args);

                        //FileHandler fh;
                        //FileHandler fh2;
                        try {
                            //fh=new FileHandler("PCCClient2.log");
                            //fh2=new FileHandler("PCEPClientParser2.log");
                            //fh.setFormatter(new SimpleFormatter());
                            //fh2.setFormatter(new SimpleFormatter());
                            //Log.addHandler(fh);
                            //Log.setLevel(Level.ALL);
                            Logger log2 = LoggerFactory.getLogger("PCEPParser");
                            //log2.addHandler(fh2);
                            //log2.setLevel(Level.ALL);
                        } catch (Exception e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                            System.exit(1);
                        }


                        String ip = args[0];
                        int port = Integer.valueOf(args[1]).intValue();

                        final QuickClientObj qcObj = new QuickClientObj(Log, ip, port);
                        if (line.hasOption("li")) {
                            qcObj.setLocalAddress(line.getOptionValue("li"));
                        }

                        qcObj.start();

			/*for multiple requests*/
                        final PCEPRequest p_r = new PCEPRequest();
                        Request req = null;
                        int i;

                        int reqIndex = 1;

                        for (i = 2; i < line.getArgs().length; i += 2) {
                            System.out.println("Creando el mensaje");
                            req = qcObj.createReqMessage(line.getArgList().get(i), line.getArgList()
                                    .get(i + 1), line, reqIndex);
                            ++reqIndex;
                            p_r.addRequest(req);
                        }

            /* Admin Group handling*/

                        if (line.hasOption("adminGroup")) {
                            UnknownObject adminGrp = new UnknownObject();
                            adminGrp.setAdminGroup(Integer.parseInt(line.getOptionValue("adminGroup")));
                            p_r.setAdminGroup(adminGrp);
                        }


            /*svec handling*/
                        Svec svec = new Svec();
                        SVECConstruct svecConstruct = new SVECConstruct();
                        ArrayList<Long> reqList = new ArrayList<Long>();

                        for (Request listIndex : p_r.getRequestList()) {
                            reqList.add(listIndex.getRequestParameters().getRequestID());
                        }
                        if (reqList.size() == 2)//multiple request lists
                            svec.setPbit(true);

                        svec.setRequestIDlist(reqList);
                        boolean flag;
                        if (line.hasOption("ndiv")) {
                            flag = Boolean.parseBoolean(line.getOptionValue("ndiv"));
                            svec.setNDiverseBit(flag);
                        }
                        if (line.hasOption("ldiv")) {
                            flag = Boolean.parseBoolean(line.getOptionValue("ldiv"));
                            svec.setLDiverseBit(flag);
                        }
                        if (line.hasOption("srlgdiv")) {
                            flag = Boolean.parseBoolean(line.getOptionValue("srlgdiv"));
                            svec.setSRLGDiverseBit(flag);
                        }

                        svecConstruct.setSvec(svec);
                        p_r.addSvec(svecConstruct);
                /*svec handling ends*/

                        System.out.println("Peticion " + req.toString());
                        final LinkedList<PCEPMessage> messageList = new LinkedList<PCEPMessage>();
                        System.out.println("Enviando mensaje");
//                        for (int ii = 0; ii < 10; ii++) {
//                            Thread thread1 = new Thread(new Runnable() {
//                                @Override
//                                public void run() {
//
//                                    int random = Calendar.getInstance().get(Calendar.MILLISECOND);
//                                    random = random + 1;
//                                    System.out.println("Random=" + random);
//                                    p_r.getRequestList().get(0).getRequestParameters().setRequestID(random);
//                                    System.out.println(
//                                            "Request is=" +
//                                                    p_r.getRequestList().get(0).getRequestParameters().getRequestID());
                                    PCEPResponse res = qcObj.sendReqMessage(p_r, messageList);
                                    System.out.println("Enviado!!!");
                                    System.out.println("Respuesta " + res);
//                                    try {
//                                        Thread.sleep(1000);
//                                    } catch (InterruptedException e) {
//                                        e.printStackTrace();
//                                    }
//                                }


//                            }
//                            );
//                            thread1.start();

//                        }

            /*single request*/
/*
            System.out.println("Creando el mensaje");
			Request req = qcObj.createReqMessage(args[2], args[3], line);
			System.out.println("Peticion "+req.toString());
			PCEPRequest p_r = new PCEPRequest();
			p_r.addRequest(req);

			LinkedList<PCEPMessage> messageList=new LinkedList<PCEPMessage>();
			System.out.println("Enviando mensaje");
			PCEPResponse res = qcObj.sendReqMessage(p_r, messageList);
			System.out.println("Enviado!!!");
			System.out.println("Respuesta "+res.toString());
*/
                    }catch (ParseException exp) {
                            System.err.println("Parsing failed.  Reason: " + exp.getMessage());
                            HelpFormatter formatter = new HelpFormatter();
                            formatter.printHelp("PCC-QuickClient <host> <port> <src> <dst> [options]", options);
                        }
        System.exit(-1);
    }


}
