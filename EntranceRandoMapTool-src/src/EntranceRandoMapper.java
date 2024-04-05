import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class EntranceRandoMapper extends JFrame {

    //Creates a JPanel that will be visible on program start.
    private JPanel WelcomePanel;

    //The program utilizes multiple tabs for pages. This creates the tabbed pane used for that functionality
    private static JTabbedPane tabbedPane;

    //ArrayList of all Points that user creates
    private ArrayList<Point> clickPoints;

    //Hashmap of what Point goes to what JPanel, so that the points show up on the correct screen
    private Map pointToPanel;

    //Instantiate the list for selecting maps to add
    public static JList listOfMaps;

    public static DefaultListModel listModel;

    private int storage = 0;

    private ArrayList<String> mapsWithContent;

    private boolean rightClickedOnMarker = false;

    private int pointerStorage = 0;

    private boolean state = false;

    private int counter = 0;

    private Map pointToPolygon;



    public EntranceRandoMapper() throws IOException {
        //Initialize the JFrame window
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setTitle("Entrance Randomizer Mapper");
        setSize(800, 600);
        setLocation(((int) screenSize.getWidth() / 2) - 400, ((int) screenSize.getHeight() / 2) - 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


        //Try to load existing map data. If data can't be found, initialize new ArrayList and HashMap.
        try {
            FileInputStream fin = new FileInputStream("src/mapdata.txt");
            ObjectInputStream oin = new ObjectInputStream(fin);
            clickPoints = (ArrayList) oin.readObject();
            pointToPanel = (HashMap) oin.readObject();
            mapsWithContent = (ArrayList) oin.readObject();
            pointToPolygon = (HashMap) oin.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("File Not Found");
            clickPoints = new ArrayList<>();
            pointToPanel = new HashMap();
            mapsWithContent = new ArrayList<String>();
            pointToPolygon = new HashMap();
        }

        //When the program closes, save all data
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                try {
                    FileOutputStream fout = new FileOutputStream("src/mapdata.txt");
                    ObjectOutputStream oout = new ObjectOutputStream(fout);
                    oout.writeObject(clickPoints);
                    oout.writeObject(pointToPanel);
                    oout.writeObject(mapsWithContent);
                    oout.writeObject(pointToPolygon);
                    oout.flush();
                    oout.close();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        JTextArea textArea = new JTextArea();
        textArea.setText("""
                Welcome!
                                
                Because of Java nonsense, I recommend maximizing this program.
                For proper functionality, make sure the program is the same window size every time you use it, otherwise
                markers may be in incorrect locations!
                                
                - Left click on an map to add a marker.
                - Backspace to remove a marker.
                - Right-click on a marker to go to its connection.
                - Middle-click to delete a set of connections.

                To add your maps, put your image files in the 'src/maps' folder, then click 'Refresh'.
                                
                Select your map you would like to add from the list, then click 'Add Map'.
                                
                """);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);

        WelcomePanel = new JPanel();
        WelcomePanel.setLayout(null);
        textArea.setBounds(20, 20, 200, 450);
        WelcomePanel.add(textArea);


        JScrollPane mapScroll = new JScrollPane();
        listModel = new DefaultListModel<>();
        listOfMaps = new JList(listModel);
        mapScroll.setViewportView(listOfMaps);
        mapScroll.setBounds(300, 20, 200, 280);
        WelcomePanel.add(mapScroll);


        JButton refresh = new JButton();
        refresh.setBounds(300, 300, 100, 30);
        refresh.setText("Refresh");
        refresh.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshMaps();
            }
        });
        WelcomePanel.add(refresh);

        JButton addMap = new JButton();
        addMap.setBounds(400, 300, 100, 30);
        addMap.setText("Add Map");
        addMap.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    addMap(null);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        WelcomePanel.add(addMap);

        tabbedPane = new JTabbedPane();

        tabbedPane.addTab("Welcome!", WelcomePanel);

        add(tabbedPane);
        setVisible(true);

        refreshMaps();


        tabbedPane.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                    if(clickPoints.isEmpty()){
                        JOptionPane.showMessageDialog(EntranceRandoMapper.this, "No markers to remove!");
                        return;
                    }
                    String mapName = (String) pointToPanel.get(clickPoints.get(clickPoints.size()-1));

                    pointToPanel.remove(clickPoints.size() - 1);
                    clickPoints.remove(clickPoints.size() - 1);

                    tabbedPane.getComponentAt(tabbedPane.indexOfTab(mapName)).repaint();
                }
            }
        });

        tabbedPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (SwingUtilities.isRightMouseButton(e)) {
                    int currentTabIndex = tabbedPane.indexAtLocation(e.getX(), e.getY());


                    ArrayList clickPointsCopy = new ArrayList(clickPoints);

                    for (int i = 0; i < clickPointsCopy.size(); i++) {

                        if (i == clickPoints.size()) break;

                        if (pointToPanel.get(clickPoints.get(i)).equals(tabbedPane.getTitleAt(currentTabIndex))) {
                            int pointer = 0;
                            if (i % 2 == 0) pointer = i + 1;
                            if (i % 2 == 1) pointer = i - 1;

                            for (Object obj : pointToPanel.keySet()) {
                                if (pointToPanel.get(obj) == null) {
                                    pointToPanel.remove(obj);
                                }
                            }
                            pointToPanel.remove(clickPoints.get(i));
                            pointToPanel.remove(clickPoints.get(pointer));

                            if (clickPoints.contains(null)) clickPoints.removeAll(Collections.singleton(null));

                            if (pointer > i) {
                                clickPoints.remove(pointer);
                                clickPoints.remove(i);
                                i--;
                            } else {
                                clickPoints.remove(i);
                                clickPoints.remove(pointer);
                                i = i - 2;
                            }

                        }
                    }

                    if (currentTabIndex > 0) tabbedPane.removeTabAt(currentTabIndex);
                    mapsWithContent.remove(currentTabIndex - 1);
                }
            }
        });


        if (mapsWithContent != null) {
            for (String mapName : mapsWithContent) {
                addMap(mapName);
            }
        }


    }


    public void addMap(String mapName) throws IOException {

        if (mapName == null) mapName = (String) listOfMaps.getSelectedValue();

        String fileNameWithoutExt = mapName.replaceFirst("[.][^.]+$", "");

        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            if (tabbedPane.getTitleAt(i).equals(fileNameWithoutExt)) {
                JOptionPane.showMessageDialog(EntranceRandoMapper.this, "Map with that name has already been added.");
                return;
            }
        }

        BufferedImage mapToAdd = ImageIO.read(new File("src/maps/" + mapName));
        ImageIcon icon = new ImageIcon(mapToAdd);

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                g.drawImage(icon.getImage(), 0, 0, getWidth(), getHeight(), this);

                for (int i = 0; i < clickPoints.size(); i++) {
                    Point p = clickPoints.get(i);
                    if (i % 2 == 0) storage++;

                    if (!pointToPanel.get(p).equals(tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()))) continue;

                    int x = (int) p.getX();
                    int y = (int) p.getY();

                    Polygon polygon = new Polygon(new int[]{x - 20, x, x + 20}, new int[]{y + 5, y - 20, y + 5}, 3);
                    pointToPolygon.put(p,polygon);

                    g.setColor(Color.decode("#A82CD8"));
                    g.fillPolygon(polygon);
                    g.setColor(Color.decode("#FDFDFD"));
                    if(storage >=100){
                        g.drawString(Integer.toString(storage), x - 9, y + 3); // Draw number
                    }else if(storage >= 10){
                        g.drawString(Integer.toString(storage), x - 6, y + 3); // Draw number
                    }else{
                        g.drawString(Integer.toString(storage), x - 3, y + 3); // Draw number
                    }
                }

                if(rightClickedOnMarker){
                    Point marker = clickPoints.get(pointerStorage);
                    if(state){
                        g.setColor(Color.BLACK);
                        g.fillPolygon((Polygon)pointToPolygon.get(marker));
                    }else{
                        g.setColor(Color.CYAN);
                        g.fillPolygon((Polygon)pointToPolygon.get(marker));
                    }



                }

                storage = 0;
            }
        };
        tabbedPane.addTab(fileNameWithoutExt, panel);
        if (!mapsWithContent.contains(mapName)) mapsWithContent.add(mapName);

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == 1) {
                    if(clickPoints.contains(e.getPoint())){
                        JOptionPane.showMessageDialog(EntranceRandoMapper.this, "There is already a marker at that exact point.");
                        return;
                    }
                    clickPoints.add(e.getPoint());
                    pointToPanel.put(e.getPoint(), tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()));
                    panel.repaint();
                } else if (e.getButton() == 2 || e.getButton() == 3) {
                    Point clicked = e.getPoint();
                    for (int i = 0; i < clickPoints.size(); i++) {

                        if(clickPoints.size() %2 != 0 && i == clickPoints.size() - 1){
                            JOptionPane.showMessageDialog(EntranceRandoMapper.this, "That marker does not have a connection.");
                            return;
                        }


                        if (((Polygon)pointToPolygon.get(clickPoints.get(i))).contains(clicked)) {
                            int pointer = 0;
                            if (i % 2 == 0) pointer = i + 1;
                            if (i % 2 == 1) pointer = i - 1;

                            if (e.getButton() == 2) {
                                for (Object obj : pointToPanel.keySet()) {
                                    if (pointToPanel.get(obj) == null) {
                                        pointToPanel.remove(obj);
                                    }
                                }
                                pointToPanel.remove(clickPoints.get(i));
                                pointToPanel.remove(clickPoints.get(pointer));

                                if (clickPoints.contains(null)) clickPoints.removeAll(Collections.singleton(null));

                                if (pointer > i) {
                                    clickPoints.remove(pointer);
                                    clickPoints.remove(i);
                                } else {
                                    clickPoints.remove(i);
                                    clickPoints.remove(pointer);
                                }

                                panel.repaint();
                                break;
                            }

                            tabbedPane.setSelectedIndex(tabbedPane.indexOfTab((String) pointToPanel.get(clickPoints.get(pointer))));
                            rightClickedOnMarker = true;
                            Timer timer = new Timer(0, new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    state = !state;
                                    repaint();
                                    counter++;
                                    if(counter == 6){
                                        ((Timer) e.getSource()).stop();
                                        rightClickedOnMarker = false;
                                        counter = 0;
                                    }
                                }
                            });
                            timer.setRepeats(true);
                            timer.setDelay(250);
                            timer.start();
                            pointerStorage = pointer;
                            break;
                        }
                    }
                }

            }
        });

    }

    public void refreshMaps() {
        File folder = new File("./src/maps");
        listModel.clear();

        if (folder.listFiles() == null) {
            JOptionPane.showMessageDialog(EntranceRandoMapper.this, "Maps not found!");
            return;
        }
        for (File map : folder.listFiles()) {
            listModel.addElement(map.getName());
        }
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new EntranceRandoMapper();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }


}
