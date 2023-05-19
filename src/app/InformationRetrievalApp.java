package app;

import main.search.Searcher;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;


@SpringBootApplication
public class InformationRetrievalApp {

	public static String getDocContext(Document d){
		return d.getFields().get(1).toString()  + d.getFields().get(4).toString() + d.getFields().get(3).toString() + d.getFields().get(7)+ "\r\n";
	}
	public static void main(String[] args) {

		SpringApplication.run(InformationRetrievalApp.class, args);
		System.setProperty("java.awt.headless", "false");
		GridBagConstraints gbc = new GridBagConstraints();
		JFrame frame=new JFrame("SONG SEARCH APP");
		frame.setVisible(true);
		frame.setResizable(false);
		frame.setSize(800,500);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JPanel panel=new JPanel();

		JTextField queryText= new JTextField();

		String[] listOfFields = {"","title","description","appears","artist","writers","producer","released"};
		JComboBox listOfOptions = new JComboBox(listOfFields);
		JComboBox listOfOptionsForOrder = new JComboBox(listOfFields);

		DefaultListModel listModel = new DefaultListModel();
		DefaultListModel historyModel = new DefaultListModel();
		JButton button=new JButton("SEARCH");
		JButton orderButton=new JButton("ORDER BY");
		JLabel queryDescription= new JLabel("ENTER KEYWORD");

		JLabel fieldDescription= new JLabel("FIELD");
		JButton nextSongBtn=new JButton("NEXT 10");
		var searchInfo = new Object() {
			private int displaySongs = 0;
			Searcher searcher = null;
			int itemsToSearch=0;
			ArrayList<Document> results = new ArrayList<>();
			HashMap<String,Integer> history = new HashMap<>();
			ArrayList<String> historyList = new ArrayList<>();
		};
		try {
			searchInfo.searcher = new Searcher("src/main");

		} catch (IOException ex) {
			ex.printStackTrace();
		}
		JList list=new JList(listModel);

		ActionListener orderHandler = o->{
			String orderedBy = String.valueOf(listOfOptionsForOrder.getSelectedItem());
			int order = 1;
			if(orderedBy.equals("title") || orderedBy.equals("")){
				order = 1;
			}
			else if(orderedBy.equals("appears")){
				order = 3;
			}
			else if(orderedBy.equals("artist")){
				order = 4;
			}
			else if(orderedBy.equals("writers")){
				order = 5;
			}
			else if(orderedBy.equals("producer")){
				order = 6;
			}
			else if(orderedBy.equals("released")){
				order = 7;
			}
			else if(orderedBy.equals("description")){
				order = 2;
			}

			searchInfo.displaySongs=0;

			for(int j = 0; j < searchInfo.itemsToSearch - 1; j++){
				for(int k = 0; k < searchInfo.itemsToSearch - j - 1; k++){
					if(String.valueOf(searchInfo.results.get(k).getFields().get(order)).compareTo(String.valueOf(searchInfo.results.get(k+1).getFields().get(order))) > 0){
						Document swap = searchInfo.results.get(k);
						searchInfo.results.set(k,searchInfo.results.get(k + 1));
						searchInfo.results.set(k+1,swap);
					}
				}
			}

			listModel.removeAllElements();


			if(searchInfo.results.size() > 10) {
				for (int j = searchInfo.displaySongs; j < searchInfo.displaySongs + 10; j++) {
					listModel.addElement(getDocContext(searchInfo.results.get(j))
							.replaceAll("stored,indexed,tokenized<title:", "")
							.replaceAll(">", " ")
							.replaceAll("stored,indexed,tokenized<released:", "")
							.replaceAll(">", "")
							.replaceAll("stored,indexed,tokenized<appears:", "")
							.replaceAll(">", " ")
							.replaceAll("stored,indexed,tokenized<artist:", "")
							.replaceAll(">", ""));
				}
			}
			else{
				for (int j = searchInfo.displaySongs; j < searchInfo.displaySongs + searchInfo.results.size(); j++) {
					listModel.addElement(getDocContext(searchInfo.results.get(j))
							.replaceAll("stored,indexed,tokenized<title:", "")
							.replaceAll(">", " ")
							.replaceAll("stored,indexed,tokenized<released:", "")
							.replaceAll(">", "")
							.replaceAll("stored,indexed,tokenized<appears:", "")
							.replaceAll(">", " ")
							.replaceAll("stored,indexed,tokenized<artist:", "")
							.replaceAll(">", ""));
				}
			}
		};

		ActionListener showHandler= h-> {
			searchInfo.displaySongs = 0;
			if(searchInfo.itemsToSearch > 10) {

				searchInfo.displaySongs += 10;
				listModel.removeAllElements();

				if((searchInfo.displaySongs+10) >= searchInfo.itemsToSearch) {
					panel.remove(nextSongBtn);
				}

				int tempSearch = 10;

				if (searchInfo.itemsToSearch - searchInfo.displaySongs < 10){
					tempSearch = searchInfo.itemsToSearch - searchInfo.displaySongs;
				}


				for (int j = searchInfo.displaySongs; j < (searchInfo.displaySongs + tempSearch); j++) {
					listModel.addElement(getDocContext(searchInfo.results.get(j))
							.replaceAll("stored,indexed,tokenized<title:", "")
							.replaceAll(">", " ")
							.replaceAll("stored,indexed,tokenized<released:", "")
							.replaceAll(">", "")
							.replaceAll("stored,indexed,tokenized<appears:", "")
							.replaceAll(">", " ")
							.replaceAll("stored,indexed,tokenized<artist:", "")
							.replaceAll(">", ""));

				}

			}

		};

		ActionListener buttonHandler= e->{
			searchInfo.displaySongs = 0;
			String query = queryText.getText();
			String field = String.valueOf(listOfOptions.getSelectedItem());
			try {
				searchInfo.results = (ArrayList<Document>) searchInfo.searcher.search(query, field);
			} catch (IOException ex) {
				ex.printStackTrace();
			} catch (ParseException ex) {
				ex.printStackTrace();
			}
			searchInfo.itemsToSearch = searchInfo.results.size();

			try {
				FileWriter myWriter = new FileWriter("search-history.txt",true);
				BufferedWriter bw = new BufferedWriter(myWriter);
				bw.write(query);
				bw.newLine();
				bw.close();
				myWriter.close();
			} catch (IOException ex) {
				System.out.println("An error occurred.");
				ex.printStackTrace();
			}

			File file = new File("search-history.txt");
			try {
				FileReader fr=new FileReader(file);
				BufferedReader br=new BufferedReader(fr);
				String line, lastSearchedKeyword = null;
				while((line=br.readLine())!=null)
				{
					if(!searchInfo.history.containsKey(line)){
						searchInfo.history.put(line,0);
					}
					lastSearchedKeyword = line;
				}

				if(searchInfo.history.containsKey(lastSearchedKeyword))
				{
					searchInfo.history.replace(lastSearchedKeyword,searchInfo.history.get(lastSearchedKeyword)+1);
				}

				fr.close();
			}
			catch (FileNotFoundException ex) {
				ex.printStackTrace();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			for(String s:searchInfo.history.keySet()){
				if(!searchInfo.historyList.contains(s)){
					searchInfo.historyList.add(s);
				}

			}

			for(int l=1; l<=searchInfo.historyList.size()-1;l++){
				for(int j=1;j<searchInfo.historyList.size()-1-l;j++){
					if(searchInfo.history.get( searchInfo.historyList.get(j-1)).compareTo(searchInfo.history.get(searchInfo.historyList.get(j)))<0){
						String temp=searchInfo.historyList.get(j-1);
						searchInfo.historyList.set(j-1,searchInfo.historyList.get(j));
						searchInfo.historyList.set(j,temp);
					}
				}
			}
			historyModel.removeAllElements();
			nextSongBtn.addActionListener(showHandler);
			orderButton.addActionListener(orderHandler);
			panel.add(nextSongBtn);
			listModel.removeAllElements();
			if(searchInfo.itemsToSearch >= 10) {
				for (int j = searchInfo.displaySongs; j < searchInfo.displaySongs + 10; j++) {
					listModel.addElement(getDocContext(searchInfo.results.get(j))
							.replaceAll("stored,indexed,tokenized<title:", "")
							.replaceAll(">", " ")
							.replaceAll("stored,indexed,tokenized<released:", "")
							.replaceAll(">", "")
							.replaceAll("stored,indexed,tokenized<appears:", "")
							.replaceAll(">", " ")
							.replaceAll("stored,indexed,tokenized<artist:", "")
							.replaceAll(">", ""));

				}
				historyModel.addElement("Most searched queries");
			}

			else {
				for (int j = searchInfo.displaySongs; j < searchInfo.itemsToSearch; j++) {
					listModel.addElement(getDocContext(searchInfo.results.get(j))
							.replaceAll("stored,indexed,tokenized<title:", "")
							.replaceAll(">", " ")
							.replaceAll("stored,indexed,tokenized<released:", "")
							.replaceAll(">", "")
							.replaceAll("stored,indexed,tokenized<appears:", "")
							.replaceAll(">", " ")
							.replaceAll("stored,indexed,tokenized<artist:", "")
							.replaceAll(">", ""));

				}
				historyModel.addElement("Most searched queries");
			}


			if(searchInfo.history.keySet().size()>=10) {
				for (int z = 0; z < 10; z++) {
					historyModel.addElement((searchInfo.historyList).get(z));
				}
			}
			else{
				for (int z = 0; z <searchInfo.history.keySet().size(); z++) {
					historyModel.addElement((searchInfo.historyList).get(z));
				}
			}
		};

		list.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent evt) {
				JList list = (JList) evt.getSource();
				if (evt.getClickCount() % 2 == 0) {

					int index = list.locationToIndex(evt.getPoint());

					for (int k = 0; k < searchInfo.itemsToSearch; k++) {
						if ((getDocContext(searchInfo.results.get(k))
								.replaceAll("stored,indexed,tokenized<title:", "")
								.replaceAll(">", " ")
								.replaceAll("stored,indexed,tokenized<released:", "")
								.replaceAll(">", "")
								.replaceAll("stored,indexed,tokenized<appears:", "")
								.replaceAll(">", " ")
								.replaceAll("stored,indexed,tokenized<artist:", "")
								.replaceAll(">", "")).equals(getDocContext(searchInfo.results.get(index))
								.replaceAll("stored,indexed,tokenized<title:", "")
								.replaceAll(">", " ")
								.replaceAll("stored,indexed,tokenized<released:", "")
								.replaceAll(">", "")
								.replaceAll("stored,indexed,tokenized<appears:", "")
								.replaceAll(">", " ")
								.replaceAll("stored,indexed,tokenized<artist:", "")
								.replaceAll(">", "")))
						{
							JTextArea area = new JTextArea(
									searchInfo.results.get(k).getFields().get(1).toString()
											.replaceAll("stored,indexed,tokenized<title:", "")
											.replaceAll(">", " ") + "\n" +
									searchInfo.results.get(k).getFields().get(2).toString()
											.replaceAll("stored,indexed,tokenized<description:", "")
											.replaceAll(">", " ") + "\n" +
									searchInfo.results.get(k).getFields().get(3).toString()
											.replaceAll("stored,indexed,tokenized<appears:", "")
											.replaceAll(">", " ")  + "\n" +
									searchInfo.results.get(k).getFields().get(4).toString()
											.replaceAll("stored,indexed,tokenized<artist:", "")
											.replaceAll(">", " ") + "\n" +
									searchInfo.results.get(k).getFields().get(5).toString()
											.replaceAll("stored,indexed,tokenized<writers:", "")
											.replaceAll(">", " ") + "\n" +
									searchInfo.results.get(k).getFields().get(6).toString()
											.replaceAll("stored,indexed,tokenized<producer:", "")
											.replaceAll(">", " ") + "\n" +
									searchInfo.results.get(k).getFields().get(7).toString()
											.replaceAll("stored,indexed,tokenized<released:", "")
											.replaceAll(">", " ")
							);

							String frameTitle =searchInfo.results.get(k).getFields().get(1).toString()
									.replaceAll("stored,indexed,tokenized<title:Title:", "")
									.replaceAll(">", " ");

							JFrame detailFrame = new JFrame(frameTitle);
							detailFrame.setVisible(true);
							detailFrame.setResizable(true);
							detailFrame.setSize(600,300);

							detailFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);


							area.setLineWrap(true);
							area.setWrapStyleWord(true);

							area.setVisible(true);
							area.setBackground(new Color(252, 206, 3));
							detailFrame.add(area);
						}
					}

				}
			};
		});

		JList historyList=new JList(historyModel);

		list.setBackground(new Color(3, 252, 198));
		button.addActionListener(buttonHandler);
		panel.setBackground(new Color(105, 66, 245));
		panel.setLayout(new GridBagLayout());
		gbc.insets=new Insets(1,1,1,1);
		gbc.gridx=0;
		gbc.gridy=0;
		gbc.fill=GridBagConstraints.HORIZONTAL;
		panel.add(queryDescription,gbc);

		gbc.gridx=0;
		gbc.gridy=1;
		gbc.fill=GridBagConstraints.HORIZONTAL;
		panel.add(queryText,gbc);

		gbc.gridx=1;
		gbc.gridy=0;
		gbc.fill=GridBagConstraints.HORIZONTAL;
		panel.add(fieldDescription,gbc);

		gbc.gridx=1;
		gbc.gridy=1;
		gbc.fill =GridBagConstraints.NONE;
		panel.add(listOfOptions,gbc);

		gbc.gridx=2;
		gbc.gridy=0;
		gbc.gridheight=2;
		gbc.fill=GridBagConstraints.VERTICAL;
		panel.add(button,gbc);


		gbc.gridx=3;
		gbc.gridy=0;
		gbc.gridheight=2;
		gbc.fill=GridBagConstraints.VERTICAL;
		panel.add(nextSongBtn,gbc);

		gbc.gridx=4;
		gbc.gridy=0;
		gbc.gridheight=1;
		gbc.fill=GridBagConstraints.HORIZONTAL;
		panel.add(orderButton,gbc);

		gbc.gridx=4;
		gbc.gridy=1;
		gbc.gridheight=1;
		gbc.fill=GridBagConstraints.HORIZONTAL;
		panel.add(listOfOptionsForOrder,gbc);


		gbc.gridx=0;
		gbc.gridy=3;
		gbc.gridwidth=10;
		gbc.fill=GridBagConstraints.HORIZONTAL;
		panel.add(list,gbc);

		gbc.gridx=0;
		gbc.gridy=6;
		gbc.gridheight=5;
		gbc.gridwidth=10;
		gbc.fill=GridBagConstraints.HORIZONTAL;
		panel.add(historyList,gbc);


		frame.add(panel);

	}
}