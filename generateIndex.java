package searchAssignment1;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

public class generateIndex {

	public static void main(String[] args) throws IOException {
		// Regex Expressions to retrieve data from trectext files.
		Pattern docNoData = Pattern.compile("<DOCNO>(.*?)</DOCNO>");
		Pattern dataLineData = Pattern.compile("<DATELINE>(.*?)</DATELINE>");
		Pattern headData = Pattern.compile("<HEAD>(.*?)</HEAD>");
		Pattern textTag = Pattern.compile("<TEXT>(.*?)</TEXT>", Pattern.DOTALL);
		Pattern byLineData = Pattern.compile("<BYLINE>(.*?)</BYLINE>");
		Pattern docTags = Pattern.compile("<DOC>(.*?)</DOC>", Pattern.DOTALL);
		
		Analyzer analyzer = new StandardAnalyzer(); // standard, keyword, stopAnalyzer, simple
		
		IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
		iwc.setOpenMode(OpenMode.CREATE);
		
		Path corpusDir = Paths.get("//Users//supreethks//Downloads//corpus");
		Directory outputFolder = FSDirectory.open(Paths.get("//Volumes//Multimedia//Search//Search//output"));
		IndexWriter writer = new IndexWriter(outputFolder,iwc);
		try(DirectoryStream<Path> allFiles = Files.newDirectoryStream(corpusDir, "*.trectext")){

			for(Path eachFile:allFiles){
				
				String documentData = new String();
				String content = new String(Files.readAllBytes(eachFile));
				
				ArrayList<String> documentList = new ArrayList<String>();
				ArrayList<String> documentList1 = new ArrayList<String>();
				ArrayList<String> docNoList = new ArrayList<String>();
				ArrayList<String> headList = new ArrayList<String>();
				ArrayList<String> blList = new ArrayList<String>();
				ArrayList<String> dlList = new ArrayList<String>();
				ArrayList<String> TextList = new ArrayList<String>();
				
				Matcher documentMatcher = docTags.matcher(content);
				getStrings(documentMatcher,2, documentList);
				
				Matcher textMatcher = textTag.matcher(documentList.get(0));
				Matcher docNoMatcher = docNoData.matcher(documentList.get(0));
				Matcher headMatcher = headData.matcher(documentList.get(0));
				Matcher dataLineMatcher = dataLineData.matcher(documentList.get(0));
				Matcher byLineMatcher = byLineData.matcher(documentList.get(0));

				for (String document: documentList) {
					
					Matcher docMatch = docTags.matcher(document);
					documentData = getStrings(docMatch,1, null);
					documentList1.add(documentData);
					
					headList.add(getStrings(headMatcher,1, null));
					blList.add(getStrings(byLineMatcher,1, null));
					dlList.add(getStrings(dataLineMatcher,1, null));
					TextList.add(getStrings(textMatcher,1, null));
					docNoList.add(getStrings(docNoMatcher,1, null));
				}
				
		    	for (int ctr = 0; ctr < TextList.size(); ctr++) {
		    		
		    		Document luceneDoc = new Document();
		    		luceneDoc.add(new StringField("DOCNO",documentList1.get(ctr),Field.Store.YES));
		    		luceneDoc.add(new TextField("TEXT",TextList.get(ctr),Field.Store.YES));
		    		writer.addDocument(luceneDoc);
		    		
		    	} 
		    	
			}
			
			writer.forceMerge(1); //Merging all the segments to one.
			writer.close();
			
			IndexReader fileIO = DirectoryReader.open(FSDirectory.open(Paths.get("//Volumes//Multimedia//Search//Search//output")));
			
			System.out.println("No of documents in the corpus: "+fileIO.maxDoc());
		    System.out.println("Number of documents containing the term \"new\" for field \"TEXT\": "+fileIO.docFreq(new Term("TEXT", "new")));
		    System.out.println("Number of occurrences of \"new\" in the field \"TEXT\": "+fileIO.totalTermFreq(new Term("TEXT","new")));
		    Terms vocabulary = MultiFields.getTerms(fileIO, "TEXT");
		    System.out.println("Size of the vocabulary for this field: "+vocabulary.size());
		    System.out.println("Number of documents that have at least one term for this field: "+vocabulary.getDocCount());
		    System.out.println("Number of tokens for this field: "+vocabulary.getSumTotalTermFreq());
		    System.out.println("Number of postings for this field: "+vocabulary.getSumDocFreq());
		    
		    TermsEnum iterator = vocabulary.iterator();
		    BytesRef byteRef = null;
		    System.out.println("\n*******Vocabulary-Start**********");
		    while((byteRef = iterator.next()) != null) {
		    	String term = byteRef.utf8ToString();
		    	System.out.print(term+"\t");
		    }
		    System.out.println("\n*******Vocabulary-End**********");
		}
	}
	
	static String getStrings(Matcher matchTerm, int value, ArrayList<String> list) {
		String foundString = new String();
		if(value == 1) {
			while(true){
				if(!matchTerm.find()) {
					break;
				}
				foundString += matchTerm.group(1);
			}
			return foundString;
		} else {
			while(true){
				if(!matchTerm.find()) {
					break;
				}
				list.add(matchTerm.group(1));
			}
			return "";
		}
	}
}
