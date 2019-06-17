package es.tid.pce.client;


import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import es.tid.pce.pcep.objects.ExcludeRouteObject;
import es.tid.pce.pcep.objects.IncludeRouteObject;
import es.tid.pce.pcep.objects.ReqAdapCap;
import es.tid.pce.pcep.objects.subobjects.IPv4PrefixXROSubobject;
import es.tid.pce.pcep.objects.subobjects.UnnumberIfIDXROSubobject;
import es.tid.pce.pcep.objects.subobjects.XROSubobject;
import es.tid.rsvp.objects.subobjects.EROSubobject;
import es.tid.rsvp.objects.subobjects.IPv4prefixEROSubobject;
import es.tid.rsvp.objects.subobjects.UnnumberIfIDEROSubobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.tid.pce.pcep.constructs.EndPoint;
import es.tid.pce.pcep.constructs.GeneralizedBandwidthSSON;
import es.tid.pce.pcep.constructs.P2PEndpoints;
import es.tid.pce.pcep.constructs.PCEPIntiatedLSP;
import es.tid.pce.pcep.constructs.Request;
import es.tid.pce.pcep.messages.PCEPInitiate;
import es.tid.pce.pcep.messages.PCEPMessage;
import es.tid.pce.pcep.messages.PCEPMessageTypes;
import es.tid.pce.pcep.messages.PCEPRequest;
import es.tid.pce.pcep.messages.PCEPResponse;
import es.tid.pce.pcep.objects.BandwidthRequested;
import es.tid.pce.pcep.objects.BandwidthRequestedGeneralizedBandwidth;
import es.tid.pce.pcep.objects.EndPoints;
import es.tid.pce.pcep.objects.EndPointsIPv4;
import es.tid.pce.pcep.objects.GeneralizedEndPoints;
import es.tid.pce.pcep.objects.LSP;
import es.tid.pce.pcep.objects.ObjectiveFunction;
import es.tid.pce.pcep.objects.P2MPEndPointsIPv4;
import es.tid.pce.pcep.objects.RequestParameters;
import es.tid.pce.pcep.objects.SRP;
import es.tid.pce.pcep.objects.tlvs.EndPointIPv4TLV;
import es.tid.pce.pcep.objects.tlvs.SymbolicPathNameTLV;
import es.tid.pce.pcep.objects.tlvs.UnnumberedEndpointTLV;
import es.tid.pce.pcepsession.PCEPSessionsInformation;
import org.apache.commons.cli.*;

public class QuickClientObj {

	private Logger log=LoggerFactory.getLogger("PCCClient");
	private String ip;
	private int port;

	private PCCPCEPSession PCEsession;

	public QuickClientObj(Logger log, String ip, int port){
		this.setIp(ip);
		this.setPort(port);
		this.log=log;
		
		PCEPSessionsInformation pcepSessionManager=new PCEPSessionsInformation();
		this.PCEsession = new PCCPCEPSession(ip, port,false,pcepSessionManager);
	}

	public QuickClientObj( String ip, int port){
		this.setIp(ip);
		this.setPort(port);
		
		PCEPSessionsInformation pcepSessionManager=new PCEPSessionsInformation();
		this.PCEsession = new PCCPCEPSession(ip, port,false,pcepSessionManager);
	}

    public static int convertIpToInt(String ipStr) {
        Inet4Address ip = null;
        try {
            ip = (Inet4Address) Inet4Address.getByName(ipStr);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        int resultIP = 0;
        if (ip != null) {
            for (byte b : ip.getAddress()) {
                resultIP = resultIP << 8 | (b & 0xFF);
            }
        }
        return resultIP;
    }




    public static CommandLine getLineOptions(String[] args) throws ParseException{
		Option gOpt = new Option("g", "Generalized end points");
		Option eroOpt = new Option("ero", "Explicit Route Object");
		Option iniOpt= new Option("ini", "Send init message");
		Option ofOpt= OptionBuilder.withArgName( "value" ).hasArg().withDescription(  "set of value" ).create( "of" );
		Option rgbwOpt= OptionBuilder.withArgName( "value" ).hasArg().withDescription(  "set rgbw value" ).create( "rgbw" );
		Option liOpt= OptionBuilder.withArgName( "value" ).hasArg().withDescription(  "local interface" ).create( "li" );
		Options options = new Options();
		options.addOption(liOpt);
		options.addOption(gOpt);
		options.addOption(eroOpt);
		options.addOption(iniOpt);
		options.addOption(ofOpt);
		options.addOption(rgbwOpt);
		CommandLineParser parser = new DefaultParser();
		
		return parser.parse( options, args );
	}

	public void setLocalAddress(String s){
		this.PCEsession.localAddress=s;
		System.out.println("local interface"+PCEsession.localAddress);
	}
	
	public void start(){
		PCEsession.start();

		try {
			System.out.println("waaait");
			PCEsession.sessionStarted.tryAcquire(15,TimeUnit.SECONDS);
			System.out.println("go go go");

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	public PCEPResponse sendReqMessage(PCEPRequest p_r, LinkedList<PCEPMessage> messageList){
		ClientRequestManager crm;
		crm=PCEsession.crm;
		PCEPResponse pr=crm.newRequest(p_r);
		messageList.add(pr);
		return pr;
	}

	public PCEPMessage sendIniMessage(PCEPInitiate p_i, LinkedList<PCEPMessage> messageList){
		ClientRequestManager crm;
		crm=PCEsession.crm;
		long maxTimeMs=15000;
		PCEPMessage pr=crm.initiate(p_i, maxTimeMs); 
		messageList.add(pr);
		return pr;
	}

	public Request createReqMessage(String src, String dst, CommandLine optArgs, int reqIndex){

		String src_ip="";;
		long src_if=0;
		long dst_if=0;
		String dst_ip="";
		
		if (src.contains(":")){
			String[] parts = src.split(":");
			src_ip=parts[0];
			src_if=Long.valueOf(parts[1]).longValue();
		}else {
			src_ip=src;
		}
		if (dst.contains(":")){
			String[] parts = dst.split(":");
			dst_ip=parts[0];
			dst_if=Long.valueOf(parts[1]).longValue();
		}else {
			dst_ip=dst;
		}
		
		
		
		
		Request req = new Request();
		RequestParameters rp= new RequestParameters();
		rp.setPbit(true);
		req.setRequestParameters(rp);		
		rp.setRequestID(PCCPCEPSession.getNewReqIDCounter());
		System.out.println("Creating test Request");
		
		int prio = 1;;
		rp.setPrio(prio);
		boolean reo = false;
		rp.setReopt(reo);
		boolean bi = false;
		rp.setBidirect(bi);
		boolean lo = false;
		rp.setLoose(lo);
		
		boolean gen=false;
		if(optArgs.hasOption("g")){
			gen=true;
			GeneralizedEndPoints ep=new GeneralizedEndPoints();
			ep.setPbit(true);
			req.setEndPoints(ep);
			P2PEndpoints p2pEndpoints = new P2PEndpoints();	
			EndPoint ep_s =new EndPoint();
			p2pEndpoints.setSourceEndpoint(ep_s);
			EndPoint ep_d =new EndPoint();
			p2pEndpoints.setDestinationEndPoints(ep_d);
			ep.setP2PEndpoints(p2pEndpoints);
			if (src_if!=0){
				UnnumberedEndpointTLV un = new UnnumberedEndpointTLV();
				Inet4Address ipp;
				try {
					ipp = (Inet4Address)Inet4Address.getByName(src_ip);
					un.setIPv4address(ipp);
					un.setIfID(src_if);
					ep_s.setUnnumberedEndpoint(un);


				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else {
				EndPointIPv4TLV ipv4tlv = new EndPointIPv4TLV();
				Inet4Address ipp;
				try {
					ipp = (Inet4Address)Inet4Address.getByName(src_ip);
					ipv4tlv.setIPv4address(ipp);
					ep_s.setEndPointIPv4TLV(ipv4tlv);

				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
			if (dst_if!=0){
				UnnumberedEndpointTLV un = new UnnumberedEndpointTLV();
				Inet4Address ipp;
				try {
					ipp = (Inet4Address)Inet4Address.getByName(dst_ip);
					un.setIPv4address(ipp);
					un.setIfID(dst_if);
					ep_d.setUnnumberedEndpoint(un);


				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else {
				EndPointIPv4TLV ipv4tlv = new EndPointIPv4TLV();
				Inet4Address ipp;
				try {
					ipp = (Inet4Address)Inet4Address.getByName(dst_ip);
					ipv4tlv.setIPv4address(ipp);
					ep_d.setEndPointIPv4TLV(ipv4tlv);

				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

			
		}else{
			EndPointsIPv4 ep=new EndPointsIPv4();
            ep.setPbit(true);
			req.setEndPoints(ep);
			//String src_ip= "1.1.1.1";
			Inet4Address ipp;
			try {
				ipp = (Inet4Address)Inet4Address.getByName(src_ip);
				ep.setSourceIP(ipp);								
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println(" - Destination IP address: ");
			//br2 = new BufferedReader(new InputStreamReader(System.in));
			//String dst_ip="172.16.101.101";
			Inet4Address i_d;
			try {
				i_d = (Inet4Address)Inet4Address.getByName(dst_ip);
				ep.setDestIP(i_d);
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}


		int of_code=-1;
		if (optArgs.hasOption("of")) {
			of_code=Integer.parseInt(optArgs.getOptionValue("of"));	
			ObjectiveFunction of=new ObjectiveFunction();
			of.setOFcode(of_code);
			of.setPbit(true);
			req.setObjectiveFunction(of);
		}
		
		
		float bw;
		int m;
		if (optArgs.hasOption("rbw"+reqIndex)){
			
			bw=Float.parseFloat(optArgs.getOptionValue("rbw"+reqIndex));
			BandwidthRequested gw = new BandwidthRequested();
			 /*convert gbps to bytes/second*/
			float bbw = ((bw*1000)/8);
			gw.setBw(bbw);
			req.setBandwidth(gw);
			
			
			
		}
		else if (optArgs.hasOption("rgbw")){		
			
			m=Integer.parseInt(optArgs.getOptionValue("rgbw"));
			BandwidthRequestedGeneralizedBandwidth gw = new BandwidthRequestedGeneralizedBandwidth();
			GeneralizedBandwidthSSON gwsson = new GeneralizedBandwidthSSON();
			gwsson.setM(m);
			gw.setGeneralizedBandwidth(gwsson);
			req.setBandwidth(gw);

		}
		String iroIp="";
		String iroIf= "";
		long iroPort;

        IncludeRouteObject iroObj = new IncludeRouteObject();
        LinkedList<EROSubobject> eroSubobjectList = new  LinkedList<EROSubobject>();

        /*Iro subobject for link*/
		if (optArgs.hasOption("iro"+ reqIndex)) {
            String iroField = optArgs.getOptionValue("iro" + reqIndex);
            iroObj.setPbit(true);
            String sip;
            Inet4Address ipFormat;
            int i;
            String iroLink = null;


            if (iroField.contains(",")) {

                String[] parts = iroField.split(",");

                for(i = 0; i <parts.length; i++) {
                    iroLink = parts[i];


                    if (iroLink.contains(":")) {
                        String[] field = iroLink.split(":");
                        iroIp = field[0];
                        iroIf = field[1];

                        //iroIf = Long.valueOf(field[1]).longValue();
                        iroPort = convertIpToInt(iroIf);

                        if (iroPort != 0) {
                            UnnumberIfIDEROSubobject unnumberSubobject =
                                    new UnnumberIfIDEROSubobject();
                            try {
                                //req.setIRO(iroObj);
                                ipFormat = (Inet4Address) Inet4Address.getByName(iroIp);
                                unnumberSubobject.setRouterID(ipFormat);
                                unnumberSubobject.setInterfaceID(iroPort);
                                unnumberSubobject.setType(4);
                                //iroObj.addIROSubobject(unnumberSubobject);
                                eroSubobjectList.add(unnumberSubobject);

                            } catch (UnknownHostException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }

                    }
                }


            }
            iroObj.setIROList(eroSubobjectList);
            req.setIRO(iroObj);
        }
        /*Iro subobject for node*/
        if (optArgs.hasOption("iron"+reqIndex)){
            String iroField = optArgs.getOptionValue("iron"+reqIndex);
            iroObj.setPbit(true);

            Inet4Address ipFormat;
            int i;
            String iroNode = null;

            if (iroField.contains(",")) {

                String[] parts = iroField.split(",");

                for(i = 0; i <parts.length; i++) {
                    iroNode = parts[i];

                    IPv4prefixEROSubobject iPv4Subobject = new IPv4prefixEROSubobject();


                    try {
                        ipFormat = (Inet4Address) Inet4Address.getByName(iroNode);
                        iPv4Subobject.setIpv4address(ipFormat);
                        iPv4Subobject.setType(1);
                        eroSubobjectList.add(iPv4Subobject);

                        //iroObj.setIROList(IPv4prefixEROSubobject);
                    } catch (UnknownHostException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

            }

            iroObj.setIROList(eroSubobjectList);
			req.setIRO(iroObj);
		}
        String xroIp= "";
        String xroIf= "";
        long xroPort;

        ExcludeRouteObject xroObj = new ExcludeRouteObject();
        LinkedList<XROSubobject> xroSubobjectList = new LinkedList<XROSubobject>();


        if (optArgs.hasOption("xro"+reqIndex)) {
            String xroField = optArgs.getOptionValue("xro"+reqIndex);
            xroObj.setPbit(true);

            Inet4Address ipFormat;
            int i;
            String xroLink = null;


            if (xroField.contains(",")) {

                String[] parts = xroField.split(",");

                for(i = 0; i <parts.length; i++) {
                    xroLink = parts[i];


                    if (xroLink.contains(":")) {
                        String[] field = xroLink.split(":");
                        xroIp = field[0];
                        xroIf = field[1];

                        //iroIf = Long.valueOf(field[1]).longValue();
                        xroPort = convertIpToInt(xroIf);

                        if (xroPort != 0) {
                            UnnumberIfIDXROSubobject unnumberSubobject =
                                    new UnnumberIfIDXROSubobject();
                            try {
                                //req.setXro(xroObj);
                                ipFormat = (Inet4Address) Inet4Address.getByName(xroIp);
                                unnumberSubobject.setRouterID(ipFormat);
                                unnumberSubobject.setInterfaceID(xroPort);
                                unnumberSubobject.setType(4);
                                //iroObj.addIROSubobject(unnumberSubobject);
                                xroSubobjectList.add(unnumberSubobject);

                            } catch (UnknownHostException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }

                    }
                }


            }
            xroObj.setXROSubobjectList(xroSubobjectList);
            req.setXro(xroObj);
        }
        if (optArgs.hasOption("xron"+reqIndex)){
            String xroField = optArgs.getOptionValue("xron"+reqIndex);
            xroObj.setPbit(true);

            Inet4Address ipFormat;
            int i;
            String xroNode = null;

            if (xroField.contains(",")) {

                String[] parts = xroField.split(",");

                for(i = 0; i <parts.length; i++) {
                    xroNode = parts[i];

                    IPv4PrefixXROSubobject iPv4Subobject = new IPv4PrefixXROSubobject();

                    try {
                        ipFormat = (Inet4Address) Inet4Address.getByName(xroNode);
                        iPv4Subobject.setIpv4address(ipFormat);
                        iPv4Subobject.setType(1);
                        xroSubobjectList.add(iPv4Subobject);

                        //iroObj.setIROList(IPv4prefixEROSubobject);
                    } catch (UnknownHostException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

            }

            xroObj.setXROSubobjectList(xroSubobjectList);
            req.setXro(xroObj);
        }


        return req;
	}

	public PCEPInitiate createIniMessage(String src, String dst, CommandLine optArgs, LinkedList<PCEPMessage> messageList){
		String src_ip="";;
		long src_if=0;
		long dst_if=0;
		String dst_ip="";
		
		if (src.contains(":")){
			String[] parts = src.split(":");
			src_ip=parts[0];
			src_if=Long.valueOf(parts[1]).longValue();
		}else {
			src_ip=src;
		}
		if (dst.contains(":")){
			String[] parts = dst.split(":");
			dst_ip=parts[0];
			dst_if=Long.valueOf(parts[1]).longValue();
		}else {
			dst_ip=dst;
		}

		boolean gen=false;
		boolean p2mp=false;
		System.out.println("origen: "+src_ip+" destino: "+dst_ip);

		
		if(optArgs.hasOption("p2mp")){
			p2mp=true;
		}

		if(optArgs.hasOption("g")){
			gen=true;
		}

		PCEPInitiate p_i = new PCEPInitiate();
		PCEPIntiatedLSP lsp_ini = new PCEPIntiatedLSP();
		p_i.getPcepIntiatedLSPList().add(lsp_ini);
		EndPoints ep;
		
		

		if (gen==true){
			ep=new GeneralizedEndPoints();
			P2PEndpoints p2pEndpoints = new P2PEndpoints();	
			EndPoint ep_s =new EndPoint();
			p2pEndpoints.setSourceEndpoint(ep_s);
			EndPoint ep_d =new EndPoint();
			p2pEndpoints.setDestinationEndPoints(ep_d);
			((GeneralizedEndPoints) ep).setP2PEndpoints(p2pEndpoints);
			if (src_if!=0){
				UnnumberedEndpointTLV un = new UnnumberedEndpointTLV();
				Inet4Address ipp;
				try {
					ipp = (Inet4Address)Inet4Address.getByName(src_ip);
					un.setIPv4address(ipp);
					un.setIfID(src_if);
					ep_s.setUnnumberedEndpoint(un);


				} catch (UnknownHostException e) {
						// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else {
				EndPointIPv4TLV ipv4tlv = new EndPointIPv4TLV();
				Inet4Address ipp;
				try {
					ipp = (Inet4Address)Inet4Address.getByName(src_ip);
					ipv4tlv.setIPv4address(ipp);
					ep_s.setEndPointIPv4TLV(ipv4tlv);

				} catch (UnknownHostException e) {
						// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
			if (dst_if!=0){
				UnnumberedEndpointTLV un = new UnnumberedEndpointTLV();
				Inet4Address ipp;
				try {
					ipp = (Inet4Address)Inet4Address.getByName(dst_ip);
					un.setIPv4address(ipp);
					un.setIfID(dst_if);
					ep_d.setUnnumberedEndpoint(un);


				} catch (UnknownHostException e) {
						// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else {
				EndPointIPv4TLV ipv4tlv = new EndPointIPv4TLV();
				Inet4Address ipp;
				try {
					ipp = (Inet4Address)Inet4Address.getByName(dst_ip);
					ipv4tlv.setIPv4address(ipp);
					ep_d.setEndPointIPv4TLV(ipv4tlv);

				} catch (UnknownHostException e) {
						// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		} else if (p2mp==true){ 
			ep=new P2MPEndPointsIPv4();
			LinkedList<Inet4Address> destIPList= new LinkedList<Inet4Address>();
			Inet4Address sourceIPP;
			Inet4Address destIPP1;
			Inet4Address destIPP2;
			Inet4Address destIPP3;
			try {
				sourceIPP = (Inet4Address)Inet4Address.getByName(src_ip);
				((P2MPEndPointsIPv4)ep).setSourceIP(sourceIPP);
				((P2MPEndPointsIPv4)ep).setLeafType(1);
				destIPP1 = (Inet4Address)Inet4Address.getByName(dst_ip);
				destIPList.add(destIPP1);
				//FIXME destIPP2 = (Inet4Address)Inet4Address.getByName(args[offset]);
				//FIXME destIPP3 = (Inet4Address)Inet4Address.getByName(args[offset+1]);
				//FIXME offset=offset+2;
				//FIXME destIPList.add(destIPP2);
				//FIXME destIPList.add(destIPP3);
				((P2MPEndPointsIPv4)ep).setDestIPList(destIPList);
			} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else {
			System.out.println("END POINTS NORMALES");
			ep=new EndPointsIPv4();				
				//String src_ip= "1.1.1.1";
			Inet4Address ipp;
			try {
				ipp = (Inet4Address)Inet4Address.getByName(src_ip);
				((EndPointsIPv4) ep).setSourceIP(ipp);								
			} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println(" - Destination IP address: ");
				//br2 = new BufferedReader(new InputStreamReader(System.in));
				//String dst_ip="172.16.101.101";
			Inet4Address i_d;
			try {
				i_d = (Inet4Address)Inet4Address.getByName(dst_ip);
				((EndPointsIPv4) ep).setDestIP(i_d);
			} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}


		lsp_ini.setEndPoint(ep);
		SRP srp= new SRP();


		if (optArgs.hasOption("srpid")){
			long srp_id = Long.valueOf(optArgs.getOptionValue("srpid")).intValue();
			srp.setSRP_ID_number(srp_id);
		}else {
			srp.setSRP_ID_number(1);
		}
		if (optArgs.hasOption("srpd")){
			srp.setRFlag(true);
		}else {
			srp.setRFlag(false);
		}

		lsp_ini.setRsp(srp);
		LSP lsp = new LSP();
		lsp_ini.setLsp(lsp);
		SymbolicPathNameTLV symPathName = new SymbolicPathNameTLV();
		lsp.setSymbolicPathNameTLV_tlv(symPathName);
		if (optArgs.hasOption("spn")){		
			symPathName.setSymbolicPathNameID(optArgs.getOptionValue("spn").getBytes());	
		}else {
			symPathName.setSymbolicPathNameID("HOLA".getBytes());
		}
		if (optArgs.hasOption("lspid")){
			int lsp_id = Integer.valueOf(optArgs.getOptionValue("lspid")).intValue();
			lsp.setLspId(lsp_id);
		}else {
			srp.setSRP_ID_number(1);
		}
		
		float bw;
		int m;
		
		if (optArgs.hasOption("rbw")){		
			bw=Float.parseFloat(optArgs.getOptionValue("rbw"));	
			BandwidthRequested gw = new BandwidthRequested();
			gw.setBw(bw);
			lsp_ini.setBandwidth(gw);
			
			
			
		}
		else if (optArgs.hasOption("rgbw")){		
			m=Integer.parseInt(optArgs.getOptionValue("rgbw"));	
			BandwidthRequestedGeneralizedBandwidth gw = new BandwidthRequestedGeneralizedBandwidth();
			GeneralizedBandwidthSSON gwsson = new GeneralizedBandwidthSSON();
			gwsson.setM(m);
			gw.setGeneralizedBandwidth(gwsson);
			lsp_ini.setBandwidth(gw);


			
		}
		
		if (optArgs.hasOption("ero")){
				System.out.println("HAY ERROO");
				PCEPResponse rep =(PCEPResponse)messageList.get(0);
				lsp_ini.setEro(rep.getResponse(0).getPath(0).geteRO());
			
		}
		return p_i;

	}

	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @param port the port to set
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * @return the ip
	 */
	public String getIp() {
		return ip;
	}

	/**
	 * @param ip the ip to set
	 */
	public void setIp(String ip) {
		this.ip = ip;
	}
}
