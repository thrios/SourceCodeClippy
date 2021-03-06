package uk.ac.glasgow.scclippy.uicomponents.search;

import com.intellij.codeInsight.actions.ReformatCodeProcessor;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;

import uk.ac.glasgow.scclippy.plugin.search.Search;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.List;


/**
 * Double Click Mouse Listener for a Post (JEditorPane)
 */
public class DoubleClickOnPostListener extends MouseAdapter {

    int id; // id of the post

    private final static String CODE_START_TAG = "<code>"; // marks snippet's start
    private final static String CODE_END_TAG = "</code>"; // marks snippet's end
    private final static int INPUT_DIALOG_MAX_SNIPPET_LENGTH = 100; // length of snippets in the JOptionPane

    JEditorPane jEditorPane;

    public DoubleClickOnPostListener(JEditorPane jEditorPane, int id) {
        this.jEditorPane = jEditorPane;
        this.id = id;
    }

    /**
     * Event handler for double click
     * Inserts selected code into the user's current editor
     * @param e the event
     */
    @Override
    public void mouseClicked(MouseEvent e) {

        // double click
        if (e.getClickCount() != 2 || Search.getFiles() == null || Search.getFiles()[id] == null) {
            return;
        }

        String text = Search.getFiles()[id].getContent();
        List<String> snippets = getSnippetsFromText(text);

        Editor editor = uk.ac.glasgow.scclippy.plugin.editor.Editor.getEditor();

        // Note: do not refactor to avoid insertion on cancel
        if (snippets.size() == 1) {
            // insert directly into code
            String insertedText = HTMLtoText(snippets.get(0));
            insertTextIntoEditor(editor, insertedText);
        }
        else if (snippets.size() > 1) {
            // ask user for input before inserting
            String chosenSnippet = getChosenSnippet(getSnippetOptions(snippets));

            if ((chosenSnippet != null) && (chosenSnippet.length() > 0)) {
                int index = indexOfChosenSnippet(chosenSnippet);
                insertTextIntoEditor(editor, HTMLtoText(snippets.get(index)));
            }
        }
    }

    /**
     * Returns the index of the chosen snippet
     * @param chosenSnippet the string of the chosen snippet
     * @return the index
     */
    private int indexOfChosenSnippet(String chosenSnippet) {
        return Integer.parseInt(chosenSnippet.substring(0, chosenSnippet.indexOf(":"))) - 1;
    }

    /**
     * Returns a list of snippets from a string based on start and end tags
     * @param text the input string
     * @return the list of snippets
     */
    private List<String> getSnippetsFromText(String text) {
        List<String> snippets = new LinkedList<>();
        int start, end = 0;
        while ((start = text.indexOf(CODE_START_TAG, end)) != -1 &&
                (end = text.indexOf(CODE_END_TAG, start)) != -1) {
            snippets.add(text.substring(start + CODE_START_TAG.length(), end));
        }

        return snippets;
    }

    /**
     * Inserts text into editor
     * @param text the text to be inserted
     */
    private void insertTextIntoEditor(Editor editor, String text) {
        if (editor == null)
            return;

        Project project = editor.getProject();
        if (project == null)
            return;

        Document doc = editor.getDocument();
        int offset = editor.getCaretModel().getOffset();

        // Write to file
        ApplicationManager.getApplication().runWriteAction(() -> {
            doc.setText(
                    doc.getText(new TextRange(0, offset)) +
                    "\n" + text +
                    doc.getText(new TextRange(offset, doc.getText().length()))
            );
        });

        // Reformat code
        PsiFile psi = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
        int snippetLength = text.length();
        TextRange range = new TextRange(offset, offset + snippetLength);
        ReformatCodeProcessor rfp = new ReformatCodeProcessor(project, psi, range, false);
        rfp.run();
    }


    /**
     * Displays a JOptionPane and asks the user for input
     * @param possibilities the options
     * @return the user's choice
     */
    public String getChosenSnippet(Object[] possibilities) {
        return (String) JOptionPane.showInputDialog(
                jEditorPane,
                "Choose which code snippet:\n",
                "Code snippet",
                JOptionPane.PLAIN_MESSAGE,
                null,
                possibilities,
                possibilities[0]);
    }

    /**
     * Takes a list of snippets and returns a formatted list of options
     * @param snippets the input snippets
     * @return the options
     */
    public Object[] getSnippetOptions(List<String> snippets) {
        Object[] snippetOptions = new Object[snippets.size()];
        for (int i = 0; i < snippets.size(); i++) {
            if (snippets.get(i).length() > 100)
                snippetOptions[i] = (i + 1) + ":" + snippets.get(i).substring(0, INPUT_DIALOG_MAX_SNIPPET_LENGTH);
            else
                snippetOptions[i] = (i + 1) + ":" + snippets.get(i);
        }

        return snippetOptions;
    }

    /**
     * Performs simple convertion of HTML to text
     * @param s html text to be converted
     * @return the text as a result
     */
    public static String HTMLtoText(String s) {
        s = s.replaceAll("&lt;", "<");
        s = s.replaceAll("&gt;", ">");
        return s;
    }
}