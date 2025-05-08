package kr.merutilm.rff.ui;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

record RFFMenuTree(List<RFFMenuTree> elements, String name, JMenuItem matcher) {

    static final class Builder {
        //type 1 : multi elements
        private final List<Builder> elements;
        private final String name;
        //type 2 : single element
        private final JMenuItem matcher;
    
        private Builder(String name){
            this.elements = new ArrayList<>();
            this.name = name;
            this.matcher = null;
        }

        private Builder(JMenuItem item){
            this.elements = Collections.emptyList();
            this.name = null;
            this.matcher = item;
        }

        public static Builder init(String name){
            return new Builder(name);
        }

        public void createMenu(String name, Consumer<Builder> build){
            Builder b = init(name);
            build.accept(b);
            elements.add(b);
        }


        public void createItem(JMenuItem item){
            elements.add(new Builder(item));
        }
        

        public RFFMenuTree build(){
            if(elements.isEmpty()){
                return new RFFMenuTree(Collections.emptyList(), null, matcher);
            }else{
                return new RFFMenuTree(elements.stream().map(Builder::build).toList(), name, null);
            }
        }
    }

    public JMenuItem createUI(){
        return createUI(true);
    }
    
    private JMenuItem createUI(boolean head){
        if(elements.isEmpty()){
            Cursor cursor = new Cursor(Cursor.HAND_CURSOR);
            matcher.setCursor(cursor);
            return matcher;
        }else{
            JMenu menu = head ? new RFFMenuBarMenu(name) : new RFFMenu(name);
            elements.forEach(e -> menu.add(e.createUI(false)));
            Cursor cursor = new Cursor(Cursor.HAND_CURSOR);
            menu.setCursor(cursor);
            return menu;
        }
    }
}
