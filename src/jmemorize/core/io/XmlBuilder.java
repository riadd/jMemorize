/*
 * jMemorize - Learning made easy (and fun) - A Leitner flashcards tool
 * Copyright(C) 2004-2008 Riad Djemili and contributors
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 1, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package jmemorize.core.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import jmemorize.core.Card;
import jmemorize.core.CardSide;
import jmemorize.core.Category;
import jmemorize.core.ImageRepository;
import jmemorize.core.Lesson;
import jmemorize.core.LessonProvider;
import jmemorize.core.Main;
import jmemorize.core.Settings;
import jmemorize.core.ImageRepository.ImageItem;
import jmemorize.core.learn.LearnHistory;
import jmemorize.core.learn.LearnHistory.SessionSummary;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author djemili
 */
public class XmlBuilder
{
    private static final String SESSION              = "session";            //$NON-NLS-1$
    private static final String LESSON               = "Lesson";             //$NON-NLS-1$
    private static final String DECK                 = "Deck";               //$NON-NLS-1$
    private static final String CARD                 = "Card";               //$NON-NLS-1$
    private static final String SIDE                 = "Side";               //$NON-NLS-1$
    private static final String IMG                  = "image";              //$NON-NLS-1$
    private static final String IMG_ID               = "id";                 //$NON-NLS-1$
    private static final String NAME                 = "name";               //$NON-NLS-1$
    private static final String CATEGORY             = "Category";           //$NON-NLS-1$
    private static final String TESTS_HIT            = "TestsHit";           //$NON-NLS-1$
    private static final String TESTS_TOTAL          = "TestsTotal";         //$NON-NLS-1$
    private static final String AMOUNT_LEARNED_BACK  = "AmountLearnedBack";  //$NON-NLS-1$
    private static final String AMOUNT_LEARNED_FRONT = "AmountLearnedFront"; //$NON-NLS-1$
    private static final String DATE_EXPIRED         = "DateExpired";        //$NON-NLS-1$
    private static final String DATE_TESTED          = "DateTested";         //$NON-NLS-1$
    private static final String DATE_TOUCHED         = "DateTouched";        //$NON-NLS-1$
    private static final String DATE_CREATED         = "DateCreated";        //$NON-NLS-1$
    private static final String DATE_MODIFIED        = "DateModified";       //$NON-NLS-1$
    private static final String BACKSIDE             = "Backside";           //$NON-NLS-1$
    private static final String FRONTSIDE            = "Frontside";          //$NON-NLS-1$
    
    private static final String STATS_ROOT           = "statistics";         //$NON-NLS-1$
    private static final String STATS_RELEARNED      = "relearned";          //$NON-NLS-1$
    private static final String STATS_SKIPPED        = "skipped";            //$NON-NLS-1$
    private static final String STATS_FAILED         = "failed";             //$NON-NLS-1$
    private static final String STATS_PASSED         = "passed";             //$NON-NLS-1$
    private static final String STATS_END            = "end";                //$NON-NLS-1$
    private static final String STATS_START          = "start";              //$NON-NLS-1$
    
    private static final String LESSON_ZIP_ENTRY_NAME = "lesson.xml";        //$NON-NLS-1$
    private static final String IMAGE_FOLDER         = "images";             //$NON-NLS-1$
    
    // we need a fixed formatter in file (not locale depent)
    private final static DateFormat DATE_FORMAT = DateFormat.getDateTimeInstance(
        DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.UK);


    
    /**
     * Saves the lesson to an {@link OutputStream} which contains an XML
     * document.
     * 
     * Don't use this method directly. Use the {@link LessonProvider} instead.
     * 
     * XML-Schema:
     * 
     * <lesson> 
     *   <deck> 
     *     <card frontside="bla" backside="bla"/> .. 
     *   </deck> ..
     * </lesson>
     */
    public static void saveAsXMLFile(File file, Lesson lesson) throws IOException, 
        TransformerException, ParserConfigurationException
    {   
        OutputStream out;
        ZipOutputStream zipOut = null;
        
        if (Settings.loadIsSaveCompressed())
        {
            out = zipOut = new ZipOutputStream(new FileOutputStream(file));
            zipOut.putNextEntry(new ZipEntry(LESSON_ZIP_ENTRY_NAME));
        }
        else
        {
            out = new FileOutputStream(file);
        }
        
        try
        {
            Document document = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder().newDocument();

            // add lesson tag as root
            Element lessonTag = document.createElement(LESSON);
            document.appendChild(lessonTag);

            // add category tags
            writeCategory(document, lessonTag, lesson.getRootCategory());
            writeLearnHistory(document, lesson.getLearnHistory());

            // transform document for file output
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8"); //$NON-NLS-1$
            transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$

            transformer.transform(new DOMSource(document), new StreamResult(out));
        }
        finally
        {
            if (zipOut != null)
                zipOut.closeEntry();
            
            else if (out != null)
                out.close();
        }
        
        try
        {
            removeUnusedImagesFromRepository(lesson);
            
            if (zipOut == null)
                writeImageRepositoryToDisk(new File(file.getParent()));
            else
                writeImageRepositoryToZip(zipOut);
        }
        finally
        {
            if (zipOut != null)
                zipOut.close();
        }
    }

    /**
     * Loads a lesson from an XML document that is contained within a file.
     * 
     * Don't use this method directly. Use the {@link LessonProvider} instead.
     * 
     * @param File xmlFile the file that containt the XML document which
     * represents the lesson.
     */
    public static void loadFromXMLFile(File xmlFile, Lesson lesson) 
        throws SAXException, IOException, ParserConfigurationException
    {
        InputStream in;
        ZipInputStream zipIn = null;
        
        try
        {
            in = new GZIPInputStream(new FileInputStream(xmlFile));
        }
        catch (IOException ex)
        {
            in = zipIn = new ZipInputStream(new FileInputStream(xmlFile));
            ZipEntry zipEntry = zipIn.getNextEntry();
            
            // file might not be compressed. try loading it directly
            if (zipEntry == null) // expected when the file is not zipped
            {
                in = new FileInputStream(xmlFile);
                zipIn = null;
            }
            else
            {
                if (!zipEntry.getName().equals(LESSON_ZIP_ENTRY_NAME))
                    throw new IOException("Unexpected zip entry.");
            }
        }
        
        // get lesson tag
        try
        {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            Document doc = factory.newDocumentBuilder().parse(in);
    
            // there must be a root category
            Element categoryTag = (Element)doc.getElementsByTagName(CATEGORY).item(0);
            loadCategory(lesson.getRootCategory(), null, categoryTag, 0);
            loadLearnHistory(doc, lesson.getLearnHistory());
        }
        finally
        {
            if (zipIn == null)
                in.close();
        }
        
        try  
        {
            if (zipIn == null)
                loadImageRepositoryFromDisk(xmlFile);
            
            else
            {
                zipIn = new ZipInputStream(new FileInputStream(xmlFile));
                
                ZipEntry entry;
                while ((entry = zipIn.getNextEntry()) != null)
                {
                    loadImageFromZipEntry(zipIn, entry);
                }
            }
        }
        catch (Exception e)
        {
            Main.logThrowable("Exception while loading lesson "+xmlFile, e);
        }
        finally
        {
            if (zipIn != null)
                zipIn.close();
        }
    }
    
    /**
     * @deprecated 
     */
    public static void loadLearnHistory(Document document, LearnHistory history)
    {
        // there must be a root category
        Element rootTag = (Element)document.getElementsByTagName(STATS_ROOT).item(0);

        if (rootTag == null)
            return;
        
        NodeList childs  = rootTag.getChildNodes();
        for(int i = 0; i < childs.getLength(); i++)
        { 
            Node child = childs.item(i);
            
            if (child.getNodeType() != Node.ELEMENT_NODE)
                continue;
            
            NamedNodeMap attributes = child.getAttributes();

            Date start = readDate(attributes, STATS_START); 
            Date end = readDate(attributes, STATS_END); 
            
            int passed = readInt(attributes, STATS_PASSED); 
            int failed = readInt(attributes, STATS_FAILED);
            int skipped = readInt(attributes, STATS_SKIPPED);
            int relearned = readInt(attributes, STATS_RELEARNED);
            
            history.addSummary(start, end, passed, failed, skipped, relearned);
        }
        
        history.setIsLoaded(true);
    }
    
    /**
     * @deprecated 
     */
    public static void writeLearnHistory(Document document, LearnHistory history)
    {
        // add lesson tag as root
        Element statsTag = document.createElement(STATS_ROOT);

        for (SessionSummary summary : history.getSummaries())
        {
            Element sessionTag = document.createElement(SESSION);
            sessionTag.setAttribute(STATS_START, DATE_FORMAT.format(summary.getStart()));
            sessionTag.setAttribute(STATS_END, DATE_FORMAT.format(summary.getEnd()));
            
            sessionTag.setAttribute(STATS_PASSED, toInteger(summary.getPassed())); 
            sessionTag.setAttribute(STATS_FAILED, toInteger(summary.getFailed()));
            sessionTag.setAttribute(STATS_SKIPPED, toInteger(summary.getSkipped()));
            sessionTag.setAttribute(STATS_RELEARNED, toInteger(summary.getRelearned()));
            
            statsTag.appendChild(sessionTag);
        }
        
        Element lessonTag = (Element)document.getElementsByTagName(LESSON).item(0);
        if (lessonTag != null)
            lessonTag.appendChild(statsTag);
        else
            document.appendChild(statsTag);
    }

    /**
     * @return the folder where images were stored (usually a dedicated
     * subfolder of given dir argument).
     */
    public static File writeImageRepositoryToDisk(File dir) throws IOException
    {
        ImageRepository repository = ImageRepository.getInstance();
        
        File imgDir = new File(dir + File.separator + IMAGE_FOLDER);
        imgDir.mkdirs();
        
        removeUnusedImages(repository, imgDir);
        
        for (ImageItem item : repository.getImageItems())
        {
            File imgFile = new File(imgDir + File.separator + item.getId());
            
            if (imgFile.exists())
            {
                // TODO if same file continue
            }
            
            FileOutputStream out = new FileOutputStream(imgFile, false);
            out.write(item.getBytes());
            out.close();
        }
        
        return imgDir;
    }

    private static void removeUnusedImages(ImageRepository repository, File imgDir)
    {
        Set<File> unusedFiles = new HashSet<File>(Arrays.asList(imgDir.listFiles()));
        
        for (ImageItem item : repository.getImageItems())
        {
            File imgFile = new File(imgDir + File.separator + item.getId());
            unusedFiles.remove(imgFile);
        }
        
        for (File unusedFile : unusedFiles)
        {
            unusedFile.delete();
        }
    }
    
    private static void writeCategory(Document document, Element father, Category category)
    {
        Element categoryTag = document.createElement(CATEGORY);
        categoryTag.setAttribute(NAME, category.getName());
        father.appendChild(categoryTag);
        
        // for all decks add a deck tag
        for (int i = 0; i < category.getNumberOfDecks(); i++)
        {
            Element deckTag = document.createElement(DECK);
            categoryTag.appendChild(deckTag);
            
            // for all cards add a card tag
            for (Card card : category.getLocalCards(i))
            {
                Element cardTag = writeCard(document, card);
                deckTag.appendChild(cardTag);
            }
        }
        
        // now add child categories
        for (Category child : category.getChildCategories())
        {
            writeCategory(document, categoryTag, child);
        }
    }

    private static Element writeCard(Document document, Card card)
    {
        Element cardTag = document.createElement(CARD);
        
        // save card sides
        cardTag.setAttribute(FRONTSIDE, card.getFrontSide().getText().getFormatted());
        cardTag.setAttribute(BACKSIDE, card.getBackSide().getText().getFormatted());
        
        // save dates
        cardTag.setAttribute(DATE_CREATED, DATE_FORMAT.format(card.getDateCreated()));
        cardTag.setAttribute(DATE_MODIFIED, DATE_FORMAT.format(card.getDateModified()));
        cardTag.setAttribute(DATE_TOUCHED, DATE_FORMAT.format(card.getDateTouched()));
        
        if (card.getDateTested() != null)
        {
            cardTag.setAttribute(DATE_TESTED, DATE_FORMAT.format(card.getDateTested()));
        }
        if (card.getDateExpired() != null)
        {
            cardTag.setAttribute(DATE_EXPIRED, DATE_FORMAT.format(card.getDateExpired()));
        }
        
        // save amount learned
        cardTag.setAttribute(AMOUNT_LEARNED_FRONT, 
            Integer.toString(card.getLearnedAmount(true)));
        
        cardTag.setAttribute(AMOUNT_LEARNED_BACK, 
            Integer.toString(card.getLearnedAmount(false)));
        
        // save stats
        cardTag.setAttribute(TESTS_TOTAL, Integer.toString(card.getTestsTotal()));
        cardTag.setAttribute(TESTS_HIT, Integer.toString(card.getTestsPassed()));
        
        // save images
        cardTag.appendChild(writeImages(document, card.getFrontSide()));
        cardTag.appendChild(writeImages(document, card.getBackSide()));
        
        return cardTag;
    }
    
    private static Element writeImages(Document doc, CardSide cardSide)
    {
        Element sideElement = doc.createElement(SIDE);
        
        for (String imgID : cardSide.getImages())
        {
            Element imgElement = doc.createElement(IMG);
            imgElement.setAttribute(IMG_ID, imgID);
            
            sideElement.appendChild(imgElement);
        }
        
        return sideElement;
    }
    
    private static void writeImageRepositoryToZip(ZipOutputStream zipOut) 
        throws IOException
    {
        ImageRepository repository = ImageRepository.getInstance();
        
        for (ImageItem item : repository.getImageItems())
        {
            zipOut.putNextEntry(new ZipEntry(IMAGE_FOLDER + File.separator + item.getId()));
            zipOut.write(item.getBytes());
            zipOut.closeEntry();            
        }
    }
    
    private static void loadCategory(Category category, Category father, 
        Element categoryTag, int depth)
    {
        // for all child tags in category tag
        int deckLevel = 0;
        NodeList childs  = categoryTag.getChildNodes();
        for (int i = 0; i < childs.getLength(); i++)
        { 
            Node child = childs.item(i);
            
            // if deck tag
            if (child.getNodeName().equalsIgnoreCase(DECK))
            {
                // for all card tags in deck tag
                NodeList childTags = child.getChildNodes();
                for (int j = 0; j < childTags.getLength(); j++)
                {
                    Node childTag = childTags.item(j);
                    
                    // if its a card child tag
                    if (!childTag.getNodeName().equalsIgnoreCase(CARD))
                        continue;
                    
                    Card card = loadCard(childTag);
                    category.addCard(card, deckLevel);
                }
                
                deckLevel++;
            }
            // if category tag
            else if (child.getNodeName().equalsIgnoreCase(CATEGORY))
            {
                Element catTag = (Element)child;
                String name = catTag.getAttribute(NAME);
                
                Category childCategory = category.getChildCategory(name);
                if (childCategory == null)
                {
                    childCategory = new Category(name);
                    category.addCategoryChild(childCategory);
                }
                
                loadCategory(childCategory, category, catTag, depth + 1);
            }
        }
    }

    private static Card loadCard(Node cardTag)
    {
        NamedNodeMap attributes = cardTag.getAttributes();
        
        // read front/backside
        String frontSide = attributes.getNamedItem(FRONTSIDE).getNodeValue();
        String backSide  = attributes.getNamedItem(BACKSIDE).getNodeValue();
        
        // read dates
        Date dateCreated  = readDate(attributes, DATE_CREATED);
        Date dateModified = readDate(attributes, DATE_MODIFIED);
        Date dateTested   = readDate(attributes, DATE_TESTED);
        Date dateExpired  = readDate(attributes, DATE_EXPIRED);
        Date dateTouched  = readDate(attributes, DATE_TOUCHED);
        
        // just to be sure
        if (dateCreated == null)
        {
            dateCreated = dateTested != null ? dateTested : new Date();
        }
        if (dateTouched == null)
        {
            dateTouched = dateTested != null ? dateTested : dateCreated;
        }
        
        // read amount learned
        int frontAmountLearned = readInt(attributes, AMOUNT_LEARNED_FRONT);
        int backAmountLearned = readInt(attributes, AMOUNT_LEARNED_BACK);
        
        // read stats
        int testsTotal = readInt(attributes, TESTS_TOTAL);
        int testsHit   = readInt(attributes, TESTS_HIT);
        
        // create card
        Card card = new Card(dateCreated, frontSide, backSide);
        if (dateModified != null)
            card.setDateModified(dateModified);
        
        card.setDateTested(dateTested);
        card.setDateExpired(dateExpired);
        card.setDateTouched(dateTouched);
        
        card.setLearnedAmount(true, frontAmountLearned);
        card.setLearnedAmount(false, backAmountLearned);
        card.incStats(testsHit, testsTotal);
        
        // load images
        card.getFrontSide().setImages(loadImages(cardTag, 0));
        card.getBackSide().setImages(loadImages(cardTag, 1));
        
        return card;
    }
    
    private static List<String> loadImages(Node cardTag, int side)
    {
        int sideIndex = 0;
        NodeList cardChildren = cardTag.getChildNodes();
        for (int i = 0; i < cardChildren.getLength(); i++)
        {
            Node sideTag = cardChildren.item(i);
            
            if (!sideTag.getNodeName().equalsIgnoreCase(SIDE))
                continue;
            
            if (side != sideIndex)
            {
                sideIndex++;
                continue;
            }
            
            NodeList childTags = sideTag.getChildNodes();
            List<String> imgIDs =  new ArrayList<String>(childTags.getLength());
            for (int j = 0; j < childTags.getLength(); j++)
            {
                Node childTag = childTags.item(j);
                
                if (!childTag.getNodeName().equalsIgnoreCase(IMG))
                    continue;
                
                Node item = childTag.getAttributes().getNamedItem(IMG_ID);
                if (item == null)
                    continue;
                
                imgIDs.add(item.getNodeValue());
            }    
            
            return imgIDs;
        }
        
        return new ArrayList<String>();
    }
    
    private static void loadImageRepositoryFromDisk(File dir)
    {
        ImageRepository repository = ImageRepository.getInstance();
        
        File imgDir = new File(dir.getParent() + File.separator + IMAGE_FOLDER);
        File[] files = imgDir.listFiles();
        
        if (files == null)
            return;
        
        for (File file : files)
        {
            try
            {
                FileInputStream in = new FileInputStream(file);
                repository.addImage(in, file.getName());
            }
            catch (FileNotFoundException e)
            {
                // ignore for now
            }
            catch (IOException e)
            {
                Main.logThrowable("could not load image "+file, e);
            }
        }
    }
    
    private static void loadImageFromZipEntry(InputStream in, ZipEntry entry) 
        throws IOException
    {
        ImageRepository repository = ImageRepository.getInstance();
        
        String name = entry.getName();
        if (!name.startsWith(IMAGE_FOLDER))
            return;
        
        repository.addImage(in, name.substring(IMAGE_FOLDER.length()+1));
    }
    
    private static void removeUnusedImagesFromRepository(Lesson lesson)
    {
        Set<String> usedImageIDs = new HashSet<String>();
        
        List<Card> allCards = lesson.getRootCategory().getCards();
        for (Card card : allCards)
        {
            usedImageIDs.addAll(card.getFrontSide().getImages());
            usedImageIDs.addAll(card.getBackSide().getImages());
        }
    
        ImageRepository.getInstance().retain(usedImageIDs);
    }

    private static String toInteger(float num)
    {
        return Integer.toString((int)num);
    }
    
    private static int readInt(NamedNodeMap attributes, String attributeItem)
    {
        Node num = attributes.getNamedItem(attributeItem);
        return (num != null) ? Integer.parseInt(num.getNodeValue()) : 0;
    }
    
    private static Date readDate(NamedNodeMap attributes, String attributeItem)
    {
        Node date = attributes.getNamedItem(attributeItem);
        
        if (date != null)
        {
            try
            {
                return DATE_FORMAT.parse(date.getNodeValue());
            }
            catch (ParseException e)
            {
                Main.logThrowable("Could not parse date.", e);
            }
        }
        
        return null;
    }
}
