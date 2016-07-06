package m.wepp;


import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.xml.sax.SAXException;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;

// 
// These are the indivdual WEPP DB records
//
public class LMODWeppData {

    List<TillageRec> tillages;
    List<VegetationRec> vegs;
    List<ResidueRec> residues;
    List<IrrigationRec> irrigations;
    List<ContourRec> contours;
    
    WEPSTags lastProcess;
    
    LMODWeppData() {
	lastProcess = new WEPSTags();
	tillages = new ArrayList<TillageRec>();
	vegs = new ArrayList<VegetationRec>();
	residues = new ArrayList<ResidueRec>();
	irrigations = new ArrayList<IrrigationRec>();
	contours = new ArrayList<ContourRec>();
	
    }
    
    // ------------- WEPS ----------------
    private class WEPSTags {
	float fuel;
	float stir;
    };
  
    //  ------------ Contours -----------------
    private class ContourRec {
	float grade;
    };
	    
    //  ------------ Residue ------------------
    private class ResidueRec {
	String cropname;
	float amount;
	boolean isAddOp;
	String weppkey;
	ResidueRec(boolean ty) {
	    isAddOp = ty;
	}
    };
    
    // ---------- Irrigation -------------------
    private class IrrigationRec {
	float amount;
    };
    
    // ---------- Tillage -----------------------
    private class TillageRec {
	float mfo1,mfo2,rho,rint,rmfo1,rmfo2,rro,surdis,tdmean;
	int numof,pcode,cltpos;
	String weppkey;
	String path,name,comment;
	
	// toString() - Returns the WEPP format of this
	public String toDBString() {
	    String rec = weppkey + "   1\n";
	    rec = rec + name + "\n";
	    rec = rec + comment + "\n";
	    rec = rec + "(from WEPP LMOD service)";
	    rec = rec + "\n";
	    rec = rec + "1 #landuse\n";
	    rec = rec + String.format("%f %f %d\n%d\n%d\n", mfo1,mfo2,numof,pcode,cltpos);
	    rec = rec + String.format("%f %f %f %f %f %f %f\n",rho,rint,rmfo1,rmfo2,rro,surdis,tdmean);
	    return rec;
	};
	
	public boolean parseTillage(Node ntill) {
	   int i,j,k;
	   
	   String elem;
	   boolean inProcess = false;
	   String value;
	   String pname; 
	  
	   Node n;
	    NodeList parms = ntill.getChildNodes();
	    for (k=0;k<parms.getLength();k++) {
		n = parms.item(k);
		if (n.getNodeType() == Node.ELEMENT_NODE) {
		    elem = n.getNodeName();
		    value = n.getTextContent();
		    switch (elem) {
			case "path":
			    path = value;
			    break;
			case "name":
			    name = value;
			    break;
			case "comment":
			    comment = value;
			    if (comment.length() > 240) {
				comment = comment.substring(0, 240);
			    }
			    break;
			case "mfo1":
			    mfo1 = Float.parseFloat(value);
			    break;
			case "mfo2":
			    mfo2 = Float.parseFloat(value);
			    break;
			case "numof":
			    numof = Integer.parseInt(value);
			    break;
			case "pcode":
			    List<String> implementCodes = Arrays.asList(new String[]{"pcodeMissing", "planter", "drill", "cultivator", "other"});
			    pcode = implementCodes.indexOf(value.toLowerCase());
			    break;
			case "cltpos":
			    List<String> cltCodes = Arrays.asList(new String[]{"front mounted","rear mounted"});
			    cltpos = cltCodes.indexOf(value.toLowerCase());
			    break;
			case "rho":
			    rho = Float.parseFloat(value);
			    break;
			case "rint":
			    rint = Float.parseFloat(value);
			    break;
			case "rmfo1":
			    rmfo1 = Float.parseFloat(value);
			    break;
			case "rmfo2":
			    rmfo2 = Float.parseFloat(value);
			    break;
			case "rro":
			    rro = Float.parseFloat(value);
			    break;
			case "surdis":
			    surdis = Float.parseFloat(value);
			    break;
			case "tdmean":
			    tdmean = Float.parseFloat(value);
			    break;
			case "weppkey":
			    weppkey = value;
			    break;
			default:
			    break;
		    }
		}
	    }
	    if ((weppkey == null) || (weppkey == "")) {
		String key = name.substring(0,6);
		key = key.toUpperCase();
		key = key.trim();
		String uniqueId = Integer.toUnsignedString(name.hashCode());
		key += "_" + uniqueId.substring(0, Math.min(5, uniqueId.length()));
		weppkey = key;
	    }
	    return true;
	}
    };
    
    // ------------- Crops ----------------------------
    private class VegetationRec {
	String path,name,comment;
	String weppkey;
	int iplant,mfocod,spriod;
	String crunit;
	float bb,bbb,beinp,btemp,cf,crit,critvm,cuthgt,decfct,diam,dlai,dropfc;
	float extnct,fact,flivmx,gddmax,hi,hmax,oratea,orater,otemp,pltol;
	float pltsp,rdmax,rsr,rtmmax,tmpmax,tmpmin,xmxlai,yld;
	
	// these are from WEPS
	String weps_hyldunits;
	float weps_tgtyield,weps_grf;
	int weps_hyldflag,weps_cbaflag;
	float weps_hmx,weps_rdmx,weps_topt,weps_hyconfact,weps_stemdia,weps_hyldwater;
	
	public String getWEPPKey() {
	    return weppkey;
	}
	public String toDBString() {
	    String rec = weppkey + "   1\n";
	    rec = rec + name + "\n";
	    rec = rec + comment.substring(0,80) + "\n";
	    rec = rec + "(from WEPP LMOD service)\n";
	    rec = rec + "comment...\n";
	    rec = rec + "1\n";
	    rec = rec + crunit + "\n";
	    rec = rec + String.format("%f %f %f %f %f %f %f %f %f %f\n",bb,bbb,beinp,btemp,cf,crit,critvm,cuthgt,decfct,diam);
	    rec = rec + String.format("%f %f %f %f %f %f %f %f\n",dlai,dropfc,extnct,fact,flivmx,gddmax,hi,hmax);
	    rec = rec + String.format("%d\n",mfocod);
	    rec = rec + String.format("%f %f %f %f %f %f %f %f %d %f\n",oratea,orater,otemp,pltol,pltsp,rdmax,rsr,rtmmax,spriod,tmpmax);
	    rec = rec + String.format("%f %f %f\n",tmpmin,xmxlai,yld);	    
	    return rec;
	}
	private boolean parseVegWepsTags(Node n) {
	    int j;
	    String elem;
	    String value;
	    
	    NodeList nl2 = n.getChildNodes();
	    for (j=0;j<nl2.getLength();j++) {
		if (nl2.item(j).getNodeType() == Node.ELEMENT_NODE) {
		    Node chld = nl2.item(j);
		    elem = chld.getNodeName();
		    value = chld.getTextContent();
		    switch (elem) {
			case "hyldunits":
			    weps_hyldunits = value;
			    break;
			case "tgtyield":
			    weps_tgtyield = Float.parseFloat(value);
			    break;
			case "grf":
			    weps_grf = Float.parseFloat(value);
			    break;
			case "hyldflag":
			    weps_hyldflag = Integer.parseInt(value);
			    break;
			case "hmx":
			    weps_hmx = Float.parseFloat(value);
			    break;
			case "rdmx":
			    weps_rdmx = Float.parseFloat(value);
			    break;
			case "topt":
			    weps_topt = Float.parseFloat(value);
			    break;
			case "cbaflag":
			    weps_cbaflag = Integer.parseInt(value);
			    break;
			case "hyconfact":
			    try {
				weps_hyconfact = Float.parseFloat(value);
			    } catch (Exception e) {
				weps_hyconfact = -999;
			    }
			    break;
			case "stemdia":
			    weps_stemdia = Float.parseFloat(value);
			    break;
			case "hyldwater":
			    weps_hyldwater = Float.parseFloat(value);
			    break;
			default:
			    break;
		    }
		}
	    }
	    return true;
	}
	public boolean parseVeg(Node nveg) {
	   int i,j,k;
	   
	   String elem;
	   String value;
	   String pname; 
	  
	    Node n;
	    NodeList parms = nveg.getChildNodes();
	    for (k=0;k<parms.getLength();k++) {
		n = parms.item(k);
		if (n.getNodeType() == Node.ELEMENT_NODE) {
		    elem = n.getNodeName();
		    value = n.getTextContent();
		    switch (elem) {
			case "wepstags":
			    parseVegWepsTags(n);
			    break;
			case "path":
			    path = value;
			    break;
			case "name":
			    name = value;
			    break;
			case "comments":
			    comment = value;
			    if (comment.length() > 240) {
				comment = comment.substring(0, 240);
			    }
			    break;
			case "weppkey":
			    weppkey = value;
			    break;
			case "iplant":
			    iplant = 1;  // always use cropland which is 1
			    break;
			case "crunit":
			    crunit = value;
			    break;
			case "bb":
			    bb = Float.parseFloat(value);
			    break;
			case "bbb":
			    bbb = Float.parseFloat(value);
			    break;
			case "beinp":
			    beinp = Float.parseFloat(value);
			    break;
			case "btemp":
			    btemp = Float.parseFloat(value);
			    break;
			case "cf":
			    cf = Float.parseFloat(value);
			    break;
			case "crit":
			    crit = Float.parseFloat(value);
			    break;
			case "critvm":
			    critvm = Float.parseFloat(value);
			    break;
			case "cuthgt":
			    cuthgt = Float.parseFloat(value);
			    break;
			case "decfct":
			    decfct = Float.parseFloat(value);
			    break;
			case "diam":
			    diam = Float.parseFloat(value);
			    break;
			case "dlai":
			    dlai = Float.parseFloat(value);
			    break;
			case "dropfc":
			    dropfc = Float.parseFloat(value);
			    break;
			case "extnct":
			    extnct = Float.parseFloat(value);
			    break;
			case "fact":
			    fact = Float.parseFloat(value);
			    break;
			case "flivmx":
			    flivmx = Float.parseFloat(value);
			    break;
			case "gddmax":
			    gddmax = Float.parseFloat(value);
			    break;
			case "hi":
			    hi = Float.parseFloat(value);
			    break;
			case "hmax":
			    hmax = Float.parseFloat(value);
			    break;
			case "mfocod":
			    List<String> mfoCodes = Arrays.asList(new String[]{"???", "fragile", "non-fragile"});
			    mfocod = mfoCodes.indexOf(value.toLowerCase());
			    break;
			case "oratea":
			    oratea = Float.parseFloat(value);
			    break;
			case "orater":
			    orater = Float.parseFloat(value);
			    break;
			case "otemp":
			    otemp = Float.parseFloat(value);
			    break;
			case "pltol":
			    pltol = Float.parseFloat(value);
			    break;
			case "pltsp":
			    pltsp = Float.parseFloat(value);
			    break;
			case "rdmax":
			    rdmax = Float.parseFloat(value);
			    break;
			case "rsr":
			    rsr = Float.parseFloat(value);
			    break;
			case "rtmmax":
			    rtmmax = Float.parseFloat(value);
			    break;
			case "spriod":
			    spriod = Integer.parseInt(value);
			    break;
			case "tmpmax":
			    tmpmax = Float.parseFloat(value);
			    break;
			case "tmpmin":
			    tmpmin = Float.parseFloat(value);
			    break;
			case "xmxlai":
			    xmxlai = Float.parseFloat(value);
			    break;
			case "yld":
			    yld = Float.parseFloat(value);
			    break;
		    }
		}
	    }
	    return true;
	}
    }  
   
    
    public String printVegs() {
	String allVegs = "";
	if (!vegs.isEmpty()) {
	    for (VegetationRec vr: vegs) {
		String rec = vr.toDBString();
		allVegs = allVegs + rec + "\n\n";
	    }
	}
	return allVegs;
    }
    public String printOps() {
	String allOps = "";
	if (!tillages.isEmpty()) {
	    for (TillageRec tr: tillages) {
		    String rec = tr.toDBString();
		    allOps = allOps + rec + "\n\n";
	    }
	}
	return allOps;
    }
    boolean parseWepsTags(Node nweps) {
	int j;
	String pname;
	String value;
	NodeList nl2 = nweps.getChildNodes();
	for (j=0;j<nl2.getLength();j++) {
	    if (nl2.item(j).getNodeType() == Node.ELEMENT_NODE) {
		Node chld = nl2.item(j);
		pname = chld.getNodeName();
		value = chld.getTextContent();
		if (pname.equals("oenergyarea")) {
		     lastProcess.fuel = Float.parseFloat(value);
		} else if (pname.equals("ostir")) {
		     lastProcess.stir = Float.parseFloat(value);
		}
	    }
	}
	return true;
    }
    
    boolean parseResidue(Node nres, boolean isadd) {
	int j;
	String pname;
	String value;
	NodeList nl2 = nres.getChildNodes();
	ResidueRec r = new ResidueRec(isadd);
	for (j=0;j<nl2.getLength();j++) {
	    if (nl2.item(j).getNodeType() == Node.ELEMENT_NODE) {
		Node chld = nl2.item(j);
		pname = chld.getNodeName();
		value = chld.getTextContent();
		if (pname.equals("plant")) {
		    r.cropname = value;
		} else if (pname.equals("amount")) {
		    r.amount = Float.parseFloat(value);
		}
	    }
	}
	residues.add(r);
	return true;
    }
   
    //
    // getVegs()
    //
    // This decodes the vegetation XML into someting WEPP can understand.
    //
    public String getVegs(String vegBlob) throws IOException, SAXException, ParserConfigurationException  {
	String vkey = null;
	if ((vegBlob != null) && (vegBlob != "")) {
	   int i,j,k;
	   DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	   DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	   InputStream inputStream = new ByteArrayInputStream(vegBlob.getBytes(Charset.forName("UTF-8")));
           Document doc = dBuilder.parse(inputStream);
	   doc.getDocumentElement().normalize();
	   NodeList nl = doc.getElementsByTagName("weppcrop");
	   Node n;
	   String elem;
	   boolean inProcess = false;
	   String value;
	   String pname; 
	   for (i=0; i<nl.getLength(); i++) {
	       n = nl.item(i);
	       elem = n.getNodeName();
	       if (elem.equals("weppcrop")) {
		   VegetationRec v = new VegetationRec();
		   v.parseVeg(n);
		   vegs.add(v);
		   vkey = v.weppkey;
	       } 
	   }
	}
	
	return vkey;
    }
    
    //
    // getProcesses()
    //
    // This builds a list of WEPP detail records that need to included to support this LMOD
    // operation.
    //
    public List<String> getProcesses(String opBlob) throws IOException, SAXException, ParserConfigurationException  {
	List<String> processes;
	processes = new ArrayList<String>();
	if ((opBlob != null) && (opBlob != "")) {
	   
	   int i,j,k;
	   DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	   DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	   InputStream inputStream = new ByteArrayInputStream(opBlob.getBytes(Charset.forName("UTF-8")));
           Document doc = dBuilder.parse(inputStream);
	   doc.getDocumentElement().normalize();
	 
	   NodeList nl = doc.getElementsByTagName("process");
	   Node n;
	   String elem;
	   boolean inProcess = false;
	   String value;
	   String pname; 
	   for (i=0; i<nl.getLength(); i++) {
		n = nl.item(i);
		elem = n.getNodeName();
		if (elem.equals("process")) {
		    NodeList n2 = n.getChildNodes();
		    for (j=0;j<n2.getLength();j++) {
			n = n2.item(j);
			if (n.getNodeType() == Node.ELEMENT_NODE) {
			    elem = n.getNodeName();
			    if (elem.equals("tillage")) {
				TillageRec tr = new TillageRec();
				tr.parseTillage(n);
				tillages.add(tr);
				processes.add(elem + "|" + tr.weppkey);
			    } else if (elem.equals("add-residue")) {
				parseResidue(n,true);
				processes.add(elem);
			    } else if (elem.equals("remove-residue")) {
				parseResidue(n,false);
				processes.add(elem);
			    } else if (elem.equals("plant")) {
				// nothing to do
				processes.add(elem);
			    } else if (elem.equals("harvest")) {
				// notthing to do
				processes.add(elem);
			    } else if (elem.equals("kill-crop")) {
				// nothing to do
				processes.add(elem);
			    }
			}
		    }
		} 
	   }
	   
	   nl = doc.getElementsByTagName("wepstags");
	   for (i=0; i<nl.getLength(); i++) {
		n = nl.item(i);
		elem = n.getNodeName();
		if (elem.equals("wepstags")) {
		    parseWepsTags(n);
		}
	   }
	}
	return processes;
    }
   
    public String getVegKey(String name) {
	for (VegetationRec vr: vegs) {
	    if (vr.name.equals(name)) {
		return vr.weppkey;
	    }
	}
	return null;
    }
    
    public float getTillDepth(String key) {
	for (TillageRec tr: tillages) {
	    if (tr.weppkey.equals(key)) {
		return tr.tdmean;
	    }
	}
	return 0;
    }
    
    public float getTillRowWidth(String key) {
	for (TillageRec tr: tillages) {
	    if (tr.weppkey.equals(key)) {
		return tr.rint;
	    }
	}
	return 0;
    }
    
    public int getVegCount() {
	return vegs.size();
    }
    
    public int getResCount() {
	return residues.size();
    }
    
    public int getContCount() {
	return contours.size();
    }
	
    public float getLastSTIR() {
	return lastProcess.stir;
    }
    public float getLastFuel() {
	return lastProcess.fuel;
    }
}
