package uk.ac.glasgow.scclippy.uicomponents.search;

import com.intellij.openapi.ui.ComboBox;
import uk.ac.glasgow.scclippy.plugin.search.*;
import uk.ac.glasgow.scclippy.uicomponents.history.SearchHistoryTab;
import uk.ac.glasgow.scclippy.uicomponents.settings.IntegerSavingJTextField;

import javax.swing.*;
import java.awt.*;

/**
 * Represents a class that contains components in the search panel/tab
 */
public class SearchTab {

    Search localSearch = new LocalIndexedSearch();
    Search webServiceSearch = new WebServiceSearch();
    Search stackExchangeSearch = new StackExchangeSearch();

    private JComponent searchPanel;
    private JScrollPane scroll;

    public static InputPane inputPane;
    public Posts posts = new Posts();

    public SearchTab(SearchHistoryTab searchHistoryTab) {
        initSearchPanel(searchHistoryTab);
    }

    void initSearchPanel(SearchHistoryTab searchHistoryTab) {
        searchPanel = new JPanel();
        searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.PAGE_AXIS));
        scroll = new SearchPanelScroll(searchPanel, posts, localSearch, webServiceSearch, stackExchangeSearch);
        inputPane = new InputPane(posts, searchHistoryTab, localSearch, webServiceSearch, stackExchangeSearch);

        String[] searchOption = {"Local Index", "Web Service", "StackExchange API"};
        JComboBox searchOptions = new ComboBox(searchOption);
        searchOptions.setSelectedIndex(1);
        searchOptions.addActionListener(e -> {
            JComboBox cb = (JComboBox)e.getSource();
            String selectedSearchOption = (String)cb.getSelectedItem();

            for (int i = 0; i < searchOption.length; i++) {
                if (searchOption[i].equals(selectedSearchOption)) {
                    Search.currentSearchType = Search.SearchType.values()[i];
                    break;
                }
            }
        });

        String[] sortOption = {"Relevance", "Score"};
        JComboBox sortOptions = new ComboBox(sortOption);
        sortOptions.setSelectedIndex(0);
        sortOptions.addActionListener(e -> {
            JComboBox cb = (JComboBox) e.getSource();
            String selectedSortOption = (String) cb.getSelectedItem();

            for (int i = 0; i < sortOption.length; i++) {
                if (sortOption[i].equals(selectedSortOption)) {
                    ResultsSorter.currentSortOption = ResultsSorter.SortType.values()[i];
                    break;
                }
            }
        });

        IntegerSavingJTextField minimumUpvotes = new IntegerSavingJTextField(ResultsSorter.minimumScore);
        int minimumUpvotesFieldSize = 3;
        minimumUpvotes.setColumns(minimumUpvotesFieldSize);
        minimumUpvotes.setToolTipText("Minimum upvotes filter of results");

        // google button
        JButton searchWithGoogleButton = new GoogleSearchButton("Google Search");
        searchWithGoogleButton.setToolTipText("Open browser to search for Stackoverflow posts");

        // top panel
        JComponent topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
        topPanel.add(searchOptions);
        topPanel.add(sortOptions);
        topPanel.add(minimumUpvotes);
        topPanel.add(searchWithGoogleButton);

        // add components to search panel
        searchPanel.add(topPanel);
        searchPanel.add(inputPane.getComponent());
        posts.addTo(searchPanel);
    }

    public Posts getPosts() {
        return posts;
    }

    public JScrollPane getScroll() {
        return scroll;
    }
}
