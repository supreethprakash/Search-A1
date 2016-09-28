import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class createIndices {
	public static void main(String[] args) throws IOException {
		// Regex Expressions to retrieve data from trectext files.
		Pattern docNoData = Pattern.compile("<DOCNO>(.*?)</DOCNO>");
		Pattern dataLineData = Pattern.compile("<DATELINE>(.*?)</DATELINE>");
		Pattern headData = Pattern.compile("<HEAD>(.*?)</HEAD>");
		Pattern textTag = Pattern.compile("<TEXT>(.*?)</TEXT>", Pattern.DOTALL);
		Pattern byLineData = Pattern.compile("<BYLINE>(.*?)</BYLINE>");
		Pattern docTags = Pattern.compile("<DOC>(.*?)</DOC>", Pattern.DOTALL);

		Analyzer analyzer = new StopAnalyzer(); // standard, keyword, stopAnalyzer, simple
		
		IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
		iwc.setOpenMode(OpenMode.CREATE);
		
		Path corpusDir = Paths.get("//Users//supreethks//Downloads//corpus");
		Directory outputFolder = FSDirectory.open(Paths.get("//Volumes//Multimedia//Search//Search//output"));
		IndexWriter writer = new IndexWriter(outputFolder,iwc);
		try(DirectoryStream<Path> allFiles = Files.newDirectoryStream(corpusDir, "*.trectext")){

			for(Path eachFile:allFiles){
				String documentData = new String();
				String content = new String(Files.readAllBytes(eachFile));
				
				ArrayList<String> textArray = new ArrayList<String>();
				ArrayList<String> documentList = new ArrayList<String>();
				ArrayList<String> documentList1 = new ArrayList<String>();
				ArrayList<String> docNoList = new ArrayList<String>();
				ArrayList<String> headList = new ArrayList<String>();
				ArrayList<String> blList = new ArrayList<String>();
				ArrayList<String> dlList = new ArrayList<String>();
				ArrayList<String> TextList = new ArrayList<String>();
				ArrayList<ArrayList<String>> total = new ArrayList<ArrayList<String>>();
				
				Matcher documentMatcher = docTags.matcher(content);
				getStrings(documentMatcher,2, documentList);
				
				Matcher textMatcher = textTag.matcher(documentList.get(0));
				Matcher docNoMatcher = docNoData.matcher(documentList.get(0));
				Matcher headMatcher = headData.matcher(documentList.get(0));
				Matcher dataLineMatcher = dataLineData.matcher(documentList.get(0));
				Matcher byLineMatcher = byLineData.matcher(documentList.get(0));

				for(String document: documentList) {
					Matcher docMatch = docTags.matcher(document);
					documentData = getStrings(docMatch,1, null);
					documentList1.add(documentData);
					
					headList.add(getStrings(headMatcher,1, null));
					blList.add(getStrings(byLineMatcher,1, null));
					dlList.add(getStrings(dataLineMatcher,1, null));
					TextList.add(getStrings(textMatcher,1, null));
					docNoList.add(getStrings(docNoMatcher,1, null));
				}
				
				total.add(documentList1);
			    total.add(headList);
			    total.add(blList);
			    total.add(dlList);
			    total.add(TextList);
				
				ArrayList<String> doc = new ArrayList<String>();
		    	ArrayList<String> file = new ArrayList<String>();
		    	doc = total.get(0);
		    	file = total.get(4);
		    	//int x = doc.size();
		    	int y = file.size();
		    	//System.out.println(x);
		    	//System.out.println(y);
		    /*	for(int j = 0; j < y;j++){
		    		Document luceneDoc = new Document();
		    		luceneDoc.add(new StringField("DOCNO",doc.get(j),Field.Store.YES));
		    		luceneDoc.add(new TextField("TEXT",file.get(j),Field.Store.YES));
		    		writer.addDocument(luceneDoc);
		    	} */
				
		    	Document luceneDoc = new Document();
				for(int ctr = 0; ctr < TextList.size(); ctr++){
					luceneDoc.add(new TextField("TEXT", TextList.get(ctr),Field.Store.YES));
				}
				for(int ctr = 0; ctr < docNoList.size(); ctr++){
					luceneDoc.add(new StringField("DOCNO",docNoList.get(0),Field.Store.YES));
				}
				for(int ctr = 0; ctr < headList.size(); ctr++){
					luceneDoc.add(new TextField("HEAD", headList.get(ctr),Field.Store.YES));
				}
				for(int ctr = 0; ctr < dlList.size(); ctr++){
					luceneDoc.add(new TextField("DATELINE", dlList.get(ctr),Field.Store.YES));
				}
				for(int ctr = 0; ctr < blList.size(); ctr++){
					luceneDoc.add(new TextField("BYLINE", blList.get(ctr),Field.Store.YES));
				} 
				writer.addDocument(luceneDoc);
			}
			writer.forceMerge(1);
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
