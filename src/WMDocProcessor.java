import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.Reader;
import java.io.StringReader;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.codehaus.stax2.XMLInputFactory2;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.process.DocumentPreprocessor;


public class WMDocProcessor {

	Pattern p1 = Pattern.compile("<ref.*?>.*?</ref>", Pattern.DOTALL);
	Pattern p2 = Pattern.compile("<ref[^<]+?/>", Pattern.DOTALL);
	Pattern p3 = Pattern.compile("\\{\\{[^{]+?\\}\\}", Pattern.DOTALL); // {{ }}
	Pattern p4 = Pattern.compile("\\{\\|.+?\\|\\}", Pattern.DOTALL); // tables
	Pattern p5 = Pattern.compile("<!\\-\\-.+?\\-\\->", Pattern.DOTALL); // comments
	Pattern p6 = Pattern.compile("\\[\\[([^|]+?)\\]\\]", Pattern.DOTALL); // Links
	Pattern p7 = Pattern.compile("\\[\\[([^]|]+?)\\|([^]|]+?)\\]\\]", Pattern.DOTALL);

	Pattern p8 = Pattern.compile("<math>.*?</math>", Pattern.DOTALL);
	Pattern p11 = Pattern.compile("<sub>(.*?)</sub>", Pattern.DOTALL); // 
	Pattern p13 = Pattern.compile("<sup>(.*?)</sup>", Pattern.DOTALL);
	Pattern p14 = Pattern.compile("<code>(.*?)</code>", Pattern.DOTALL);
	
	Pattern p9 = Pattern.compile("<blockquote>.*?</blockquote>", Pattern.DOTALL);
	Pattern p10 = Pattern.compile("\\[http:[^]]+?\\]", Pattern.DOTALL);
	
	Pattern p12 = Pattern.compile("\\([^(]+?\\)", Pattern.DOTALL); // Brackets
	
	Pattern p15 = Pattern.compile("\\s(?>-?\\d+(?:[\\./]\\d+)?)\\s", Pattern.DOTALL); // Numbers
	Pattern p16 = Pattern.compile("^(?>-?\\d+(?:[\\./]\\d+)?)\\s", Pattern.DOTALL); // Numbers
	
	BufferedWriter bw = null;
	String prefix;
	int numDocs = 0;
	int numCol = 0;
	
	StringBuilder builder;
	
	void init(String _prefix) {
		prefix = _prefix;
		try {
			String filePath = prefix+"_"+numCol+".txt";
			bw = new BufferedWriter(new FileWriter(filePath));
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	void cleanUp() {
		try {
			bw.close();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	String removeRegex(String s, Pattern p) {

		builder = new StringBuilder();
		
		Matcher m = p.matcher(s);
		
		int matches = 0;
		int endDif, end = 0;
		
        while (m.find()) { 
        	builder.append(s.substring(end, m.start()));
        	end = m.end();
        	matches++;        	
        	//System.out.println(s.substring(m.start(), m.end()));
        }
        if (matches>0) builder.append(s.substring(end));
        
        if (matches==0) return s;
        
        //endDif = s.length()-end;
        //if (endDif<touchOffset) touchOffset = endDif;
        return builder.toString();
	}
	
	String removeRegexGroup(String s, Pattern p) {

		builder = new StringBuilder();
		
		Matcher m = p.matcher(s);
		
		int matches = 0;
		int endDif, end = 0;
		
        while (m.find()) { 
        	builder.append(s.substring(end, m.start()));
        	builder.append(s.substring(m.start()+2, m.end()-2));        	
        	end = m.end();
        	matches++;        	
        	//System.out.println(s.substring(m.start()+2, m.end()-2));
        }
        if (matches>0) builder.append(s.substring(end));
        
        if (matches==0) return s;
        
        //endDif = s.length()-end;
        //if (endDif<touchOffset) touchOffset = endDif;
        return builder.toString();
	}
	
	String removeRegexGroup2(String s, Pattern p, int grp) {

		builder = new StringBuilder();
		
		Matcher m = p.matcher(s);
		
		int matches = 0;
		int endDif, end = 0;
		
        while (m.find()) { 
        	builder.append(s.substring(end, m.start()));
        	builder.append(m.group(grp));        	
        	end = m.end();
        	matches++;        	
        	//System.out.println(m.group(grp));
        }
        if (matches>0) builder.append(s.substring(end));
        
        if (matches==0) return s;
        
        //endDif = s.length()-end;
        //if (endDif<touchOffset) touchOffset = endDif;
        return builder.toString();
	}
	
	
	String processDoc(String str) {
		
		String s;

        s = removeRegex(str, p5);
        
        s = removeRegex(s, p3);
        s = removeRegex(s, p3);
        
        s = removeRegex(s, p4);
        s = removeRegex(s, p2);
        s = removeRegex(s, p1);

        s = removeRegexGroup(s, p6);
        s = removeRegexGroup2(s, p7, 2);
        
        s = removeRegex(s, p8);
        s = removeRegexGroup2(s, p11, 1);
        s = removeRegexGroup2(s, p13, 1);
        
        s = removeRegex(s, p9);
        
        s = removeRegex(s, p10);

        s = removeRegex(s, p12);
        s = removeRegex(s, p14);
        
        s = s.replaceAll("&nbsp;", " ");
        
        String[] strs = s.split("\n");
        String[] strs2;
        
        builder = new StringBuilder();
        
        for (String l : strs) {
        	if (l.startsWith("File:")) continue;
        	if (l.startsWith("[[File:")) continue;
        	if (l.startsWith("[[Image:")) continue;
        	if (l.startsWith("[[Image:")) continue;
        	if (l.startsWith("Category:")) continue;
        	if (l.startsWith("<gallery")) continue;
        	if (l.startsWith("*")) continue;
        	if (l.startsWith(":")) continue;
        	if (l.startsWith("=")) continue;
        	if (l.startsWith("#")) continue;
        	if (l.startsWith("|")) continue;
        	
        	strs2 = l.split(" ");
        	if (strs2.length<5) continue;
        	
        	builder.append(l+"\n");
        }
        
        s = builder.toString();
        s = s.replaceAll("'+", "'");
        s = s.replaceAll("\"+", "\"");
        
        return s;
        
	}	
	
	
	String replaceNumbers(String s, Pattern p) {
		
		String s1, s2;
		Matcher m = p.matcher(s);
		
		int matches = 0;
		int endDif, end = 0;
		
		int cursor = 0;
		
		builder = new StringBuilder();
		
        while (m.find()) { 
        	
        	matches++;
        	
        	// Get the substring where the numbers are
        	s1 = s.substring(m.start(), m.end());
        	s1 = s1.replaceAll("\\d", "0");
        	
        	// Accumulate parts of the string which have been processed
        	builder.append(s.substring(cursor,m.start()));
        	builder.append(s1);
        	
        	cursor = m.end();      	
        }
        
        if (matches==0) return s;
        else {
        	builder.append(s.substring(cursor, s.length()));
        	return builder.toString();
        }        
	}
	
	
	String normalizeText(String str) {
		
		String r;

		r = str.replaceAll("\\s's\\s", "'s ");	
		r = r.replaceAll("[,:/`\"]", "");	
		r = r.replaceAll("''", "");
		r = r.replaceAll("\\s'\\s", " ");		
		r = Normalizer.normalize(r, Normalizer.Form.NFD);
		r = r.replaceAll("[^\\p{ASCII}]", "");
		
		// Numbers
		//r = r.replaceAll("\\s(?>-?\\d+(?:[\\./]\\d+)?)\\s", " 0 ");
		r = replaceNumbers(r, p15);
		r = replaceNumbers(r, p16);
		
		r = r.toLowerCase();
		r = r.replaceAll("<br>", " ");
		r = r.replaceAll("-lsb-", "");
		r = r.replaceAll("-rsb-", "");
		r = r.replaceAll("-lrb-", "");
		r = r.replaceAll("-rrb-", "");		
		r = r.replaceAll("\\s+", " ");
		r = r.replaceAll("\\?", "");
		r = r.replaceAll("\\.\\.\\.", "");
		
		r = r.trim();
		
		return r;
		
	}
	
	void addDocument(String title, String text) {
			
		//if (text.contains("his can be explained")) {			
		
		String s, s2;
		String[] strs, strs2;
		ArrayList<String> sList = new ArrayList<String>();
				
		numDocs++;		
		try {
			 s = processDoc(text);
			
			if (s.trim().length()<30) return;
			
			segment(s, sList);
			
			bw.write("\n\n############");
			bw.write("\n"+title.toLowerCase()+"\n");
			
			for (String line : sList) {				
				
				strs = line.split(";");
				
				for (String seg : strs) {
					
					s2 = normalizeText(seg);
					strs2 = s2.split(" ");
					if (seg.length()>30 && strs2.length>4) bw.write( s2 +"\n");
				}				
			}
			
			//bw.write("\n"+s);
			
			
		}
		catch(Exception e) {
			e.printStackTrace();
		}			
		
		if (numDocs>10000) {
			numCol++;
			numDocs = 0;
			String filePath = prefix+"_"+numCol+".txt";
			try {
				bw.close();
				bw = new BufferedWriter(new FileWriter(filePath));
			}
			catch(Exception e) {
				e.printStackTrace();
			}			
		}		
	}
	
	
	void segment(String str, ArrayList<String> sList) {
		Reader reader = new StringReader(str);
		DocumentPreprocessor dp = new DocumentPreprocessor(reader);
		Iterator<List<HasWord>> it = dp.iterator();		
		while (it.hasNext()) {
		   StringBuilder sentenceSb = new StringBuilder();
		   List<HasWord> sentence = it.next();
		   
		   
		   //-------------------------------
		   // Condition on sentence length
		   //-------------------------------
		   if (sentence.size()<5) continue;
		   
		   
		   for (HasWord token : sentence) {
		      if(sentenceSb.length()>0) {
		         sentenceSb.append(" ");
		      }
		      sentenceSb.append(token);
		   }
		   sList.add(sentenceSb.toString());
		}
	}
	
	
	void parse(String filePath) {		
			
		try {
			FileInputStream fin = new FileInputStream(filePath);
			BufferedInputStream in = new BufferedInputStream(fin);
			BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(in);
			
			//InputStream is = new ByteArrayInputStream(xml.getBytes("UTF-8"));
			XMLInputFactory2 xi = (XMLInputFactory2)XMLInputFactory2.newInstance();

			xi.setProperty(XMLInputFactory.SUPPORT_DTD,	Boolean.FALSE);
			xi.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES ,	Boolean.TRUE);

			XMLStreamReader reader = xi.createXMLStreamReader(bzIn);
			
			String l = null;	
			String title = "";
			String text;
			int type;
			int count = 0;
			
			StringBuffer buf = new StringBuffer();
			
			boolean use;
			
			while (reader.hasNext()) {				
				
				type = reader.next();	
				if (type==1) {
					l = reader.getLocalName();						
					if (l.equals("title")) {
						
						title = reader.getElementText();						
					}
					if (l.equals("text")) {
						
						if (title.startsWith("Category:") || title.startsWith("File:") ||
								title.startsWith("Wikipedia:") || title.startsWith("Template:")) continue;
						
						addDocument(title, reader.getElementText());
						/*if (count>200000) {
							addDocument(title, reader.getElementText());
						}*/
						count++;
						if (count%1000==0) {
							System.out.println(count+"  "+title);
						}
						//System.out.println(reader.getElementText());
						//if (count>5050) break;
					}
				}
				
		    }
		    reader.close();
		}
		catch(Exception e) {
			e.printStackTrace();
			//if (!e.getLocalizedMessage().startsWith("Unexpected character")) e.printStackTrace();
			//System.out.println(e.getLocalizedMessage());
		}		
	}
	
	
	// To Do
	
	// Need to remove lines with less than 5 tokens
	
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		WMDocProcessor dp = new WMDocProcessor();
		String filePath = "C:/Temp/enwiki-latest-pages-articles.xml.bz2";
		
		dp.init("C:/Temp/wiki_out");
		dp.parse(filePath);
		dp.cleanUp();
	}

}
