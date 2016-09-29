package searchAssignment1;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
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

public class indexComparison {


	public static void main(String[] args) throws IOException {
		// Regex Expressions to retrieve data from trectext files.
		Pattern docNoData = Pattern.compile("<DOCNO>(.*?)</DOCNO>");
		Pattern dataLineData = Pattern.compile("<DATELINE>(.*?)</DATELINE>");
		Pattern headData = Pattern.compile("<HEAD>(.*?)</HEAD>");
		Pattern textTag = Pattern.compile("<TEXT>(.*?)</TEXT>", Pattern.DOTALL);
		Pattern byLineData = Pattern.compile("<BYLINE>(.*?)</BYLINE>");
		Pattern docTags = Pattern.compile("<DOC>(.*?)</DOC>", Pattern.DOTALL);
		
		Scanner input = new Scanner(System.in);
		System.out.println("Enter which Analyzer to use\n 1. Standard \n 2. Simple\n 3. Keyword \n 4. Stop\n");
		System.out.println("(Enter 1, 2, 3 or 4)\n");
		int option = input.nextInt();
		Analyzer analyzer;
		if(option == 1){
			analyzer = new StandardAnalyzer();
		} else if (option == 3) {
			analyzer = new KeywordAnalyzer();
		} else if (option == 2) {
			analyzer = new SimpleAnalyzer();
		} else {
			analyzer = new StopAnalyzer(); 
		}
		
		System.out.println("Please wait. Process is going on\n");
		
		IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
		iwc.setOpenMode(OpenMode.CREATE);
		
		Path corpusDir = Paths.get("src//corpus");
		Directory outputFolder = FSDirectory.open(Paths.get("src//output"));
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

				for (String document: documentList) {
					
					Matcher textMatcher = textTag.matcher(document);
					Matcher docNoMatcher = docNoData.matcher(document);
					Matcher headMatcher = headData.matcher(document);
					Matcher dataLineMatcher = dataLineData.matcher(document);
					Matcher byLineMatcher = byLineData.matcher(document);
					
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
			
			IndexReader fileIO = DirectoryReader.open(FSDirectory.open(Paths.get("src//output")));
			
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
