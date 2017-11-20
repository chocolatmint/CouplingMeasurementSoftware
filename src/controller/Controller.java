package controller;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import model.grammar.*;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

/**
 *
 * @author Maudy
 */
public class Controller {

    private String[] keyword = {"break;", "catch(", "continue;", "do{", "else{", "finally{", "for(", "if(", "super(", "switch(", "try{", "while("};
    private HashSet<String> keywordJava = new HashSet<>(Arrays.asList(keyword));

    private int nilaiCBO;
    private int nilaiDIT;
    private int nilaiLCOM;
    private int nilaiNOC;
    private int nilaiRFC;

    public Set<String> analyze(String input, HashMap<String, String> argumen, HashMap<String, HashMap<String, Set<String>>> namaLocVar, HashMap<String, Set<String>> insVar) {
        Stack<String> stack = new Stack<>();
        Stack<String> stack2 = new Stack<>();
        HashMap<String, Set<String>> tempKelasNew = new HashMap<>();
        Set<String> tempInsVar = new HashSet<>();
        Set<String> hasil = new HashSet<>();
        String[] pisahExp = input.split("[;{}]");
        for (int i = 0; i < pisahExp.length; i++) {
            if (!pisahExp[i].equals("")) {
                Iterator<String> itAnalyze = keywordJava.iterator();
                while (itAnalyze.hasNext()) {
                    boolean titikDua = false;
                    String javaKeywords = itAnalyze.next();
                    if (pisahExp[i].contains(javaKeywords)) {
                        pisahExp[i] = pisahExp[i].replace(javaKeywords, "");
                        if (pisahExp[i].endsWith(")")) {
                            pisahExp[i] = pisahExp[i].replace(")", "");
                        }
                    }
                    if (pisahExp[i].contains("this.") && pisahExp[i].contains("=new")) {
                        String[] tempKalimat = pisahExp[i].split("=new");
                        tempKalimat[0] = tempKalimat[0].replaceAll("this.", "");
                        if (tempKelasNew.get(tempKalimat[0]) != null) {
                            Set<String> tempIV = tempKelasNew.get(tempKalimat[0]);
                            tempIV.add(tempKalimat[tempKalimat.length - 1]);
                            tempKelasNew.put(tempKalimat[0], tempIV);
                        } else {
                            tempInsVar.add(tempKalimat[tempKalimat.length - 1]);
                            tempKelasNew.put(tempKalimat[0], tempInsVar);
                        }
                    }
                    if (pisahExp[i].contains(":")) {
                        String[] pisahExpTitikDua = pisahExp[i].split(":");
                        for (int j = 0; j < pisahExpTitikDua.length; j++) {
                            if (!pisahExpTitikDua[j].equals("")) {
                                stack.push(pisahExpTitikDua[j]);
                                titikDua = true;
                            }
                        }
                    }
                    if (titikDua == false) {
                        stack.push(pisahExp[i]);
//                        System.out.println(pisahExp[i]);
                    }
                }
            }
        }

        while (!stack.empty()) {
            String peek = stack.peek();
            if (peek.contains("this.")) {
                if (!peek.contains("=new")) {
                    String[] splitAllChar = peek.split("[^a-zA-Z0-9.]");
                    for (int i = 0; i < splitAllChar.length; i++) {
                        if (splitAllChar[i].contains(".")) {
                            Stack<String> stackCopy = (Stack<String>) stack.clone();
                            Stack<String> stackCopy2 = (Stack<String>) stack2.clone();
                            hasil.addAll(this.titikHasil(stackCopy, stackCopy2, argumen, namaLocVar, insVar, tempKelasNew, splitAllChar[i]));
                        }
                    }
                }
            } else if (peek.contains("].")) {
                List<String> matchList = new ArrayList<>();
                Pattern regex = Pattern.compile("(?<=\\[).*?(?=\\])");
                Matcher regexMatcher = regex.matcher(peek);

                while (regexMatcher.find()) {//Finds Matching Pattern in String
                    matchList.add(regexMatcher.group());//Fetching Group from String
                }

                for (String str : matchList) {
                    if (!str.equals("")) {
                        if (str.contains(".")) {
                            String[] splitAllChar = str.split("[^a-zA-Z0-9.]");
                            for (int i = 0; i < splitAllChar.length; i++) {
                                if (splitAllChar[i].contains(".")) {
                                    Stack<String> stackCopy = (Stack<String>) stack.clone();
                                    Stack<String> stackCopy2 = (Stack<String>) stack2.clone();
                                    hasil.addAll(this.titikHasil(stackCopy, stackCopy2, argumen, namaLocVar, insVar, tempKelasNew, splitAllChar[i]));
                                }
                            }
                        }
                    }
                }

                peek = peek.replaceAll("\\[(.*?)\\]", "");
                if (peek.contains(".")) {
                    String[] splitAllChar = peek.split("[^a-zA-Z0-9.]");
                    for (int i = 0; i < splitAllChar.length; i++) {
                        if (splitAllChar[i].contains(".")) {
                            Stack<String> stackCopy = (Stack<String>) stack.clone();
                            Stack<String> stackCopy2 = (Stack<String>) stack2.clone();
                            hasil.addAll(this.titikHasil(stackCopy, stackCopy2, argumen, namaLocVar, insVar, tempKelasNew, splitAllChar[i]));
                        }
                    }
                }
            } else if (peek.contains(".")) {
                String[] splitAllChar = peek.split("[^a-zA-Z0-9.]");
                for (int i = 0; i < splitAllChar.length; i++) {
                    if (splitAllChar[i].contains(".")) {
                        Stack<String> stackCopy = (Stack<String>) stack.clone();
                        Stack<String> stackCopy2 = (Stack<String>) stack2.clone();
                        hasil.addAll(this.titikHasil(stackCopy, stackCopy2, argumen, namaLocVar, insVar, tempKelasNew, splitAllChar[i]));
                    }
                }
            }
            stack2.push(stack.pop());
        }
        return hasil;
    }

    public void getKelas(ArrayList<String> pathKelas, ArrayList<String> namaKelas) throws IOException, ClassNotFoundException {
        Set<String> hashSetRFC = new HashSet<>();
        HashMap<String, Integer> hashMapCBO = new HashMap<>();
        HashMap<String, Integer> hashMapLCOM = new HashMap<>();

        int counterNOC = 0;
        int counterRFC = 0;
        int counterCBO = 0;
        int counterLCOM = 0;
        int counterDIT = 0;
        for (String path : pathKelas) {
            ANTLRInputStream input = new ANTLRInputStream(new FileInputStream(path));
            JavaLexer lexer = new JavaLexer(input);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            JavaParser parser = new JavaParser(tokens);
            JavaParser.CompilationUnitContext tree = parser.compilationUnit();
            for (String namaKela : namaKelas) {
                /**
                 * NOC
                 */
                ListenerNOC objekNOC = new ListenerNOC();
                ParseTreeWalker.DEFAULT.walk(objekNOC, tree);

                if (objekNOC.getSuperclassName() != null && objekNOC.getSuperclassName().equals(namaKela)) {
                    counterNOC++;
                    /**
                     * DEBUG
                     */
//                    System.out.println(path); //nama kelas yang sedang diobservasi
//                    System.out.println(namaKela); //nama kelas yang sedang diiterasi
//                    System.out.println(objekNOC.getSuperclassName()); //nama superclass dari kelas yang sedang diobservasi
                }//beres itung NOC
            }
//                --------------------------------------------------------------
            /**
             * RFC
             */
            ListenerRFC objekRFC = new ListenerRFC();
            ParseTreeWalker.DEFAULT.walk(objekRFC, tree);

            Set<String> methodInvocation = new HashSet<String>();
            Stack<String> stackPisahMethod = new Stack<>();
//            for (int j = 0; j < objekRFC.getNamaMethodInvocation().size(); j++) {
//                String[] a = objekRFC.getNamaMethodInvocation().get(j).split("\\.");
//                String temp = "";
//                for (int k = 0; k < a.length; k++) {
//                    if (a[k].contains("(")) {
//                        stackPisahMethod.push(a[k]);
//                    }
//                    if (a[k].contains(")")) {
//                        temp = stackPisahMethod.pop();
//                        temp = temp.substring(0, temp.indexOf("("));
//                        methodInvocation.add(temp);
////                        System.out.println("temp "+temp);
//                    }
//                    temp = "";
//                }
//            }

            Iterator<String> it = objekRFC.getNamaMethodInvocation().iterator();
            while (it.hasNext()) {
                String[] a = it.next().split("\\.");
                String temp = "";
                for (int k = 0; k < a.length; k++) {
                    if (a[k].contains("(")) {
                        stackPisahMethod.push(a[k]);
                    }
                    if (a[k].contains(")")) {
                        temp = stackPisahMethod.pop();
                        temp = temp.substring(0, temp.indexOf("("));
                        methodInvocation.add(temp);
                    }
                    temp = "";
                }
            }

            Set<String> methodRFC = new HashSet<>();
            methodRFC.addAll(methodInvocation);
            methodRFC.addAll(objekRFC.getNamaMethod());

            counterRFC += methodRFC.size();
            /**
             * DEBUG
             */
//                Iterator<String> its = methodRFC.iterator();
//                while (its.hasNext()) {
//                    System.out.println("method invocation: " + its.next());
//                } //beres itung RFC
//                --------------------------------------------------------------
            /**
             * CBO
             */
            ListenerCBO objekCBO = new ListenerCBO();
            ParseTreeWalker.DEFAULT.walk(objekCBO, tree);

            Set<String> kelasCBO = new HashSet<>();
            HashMap<String, HashMap<String, Set<String>>> namaLocVar = (objekCBO.getNamaTipeLocalVar());
            HashMap<String, Set<String>> namaInstaVar = (objekCBO.getListInstanceVariable());
            String inputIsiMethod = "";
            HashMap<String, String> hmapVarLokalParam = new HashMap<>();
            Iterator<String> itcbo = objekCBO.getMethodIsi().keySet().iterator();
            Set<String> hasilAnalyze = new HashSet<>();

            while (itcbo.hasNext()) {
                String metName = itcbo.next();
//                System.out.println("CBO. namaMethod: " + metName);
                //jangan ambil kalo methodnya gak punya param
                if (objekCBO.getListMethodParamType().get(metName) != null) {
                    //masukin nama param yang bertipe kelas
                    hmapVarLokalParam = objekCBO.getListMethodParamType().get(metName);
                }
                inputIsiMethod = objekCBO.getMethodIsi().get(metName);
                //ngeganti setiap literal jadi string kosong
                Iterator<String> itcbo2 = objekCBO.getListLiteral().iterator();
                while (itcbo2.hasNext()) {
                    String literal = itcbo2.next();
                    if (inputIsiMethod.contains(literal)) {
                        inputIsiMethod = inputIsiMethod.replaceAll(literal, "");
                    }
                }
                //masukin buat dianalisis
                hasilAnalyze = this.analyze(inputIsiMethod, hmapVarLokalParam, namaLocVar, namaInstaVar);
                Iterator<String> itcbo3 = hasilAnalyze.iterator();

                while (itcbo3.hasNext()) {
                    String tempHasil = itcbo3.next();
                    String[] splitKarakter = tempHasil.split("[^a-zA-Z0-9]");
                    for (int i = 0; i < splitKarakter.length; i++) {
                        if (!splitKarakter[i].matches("[-+]?\\d*\\.?\\d+")) {
                            if (!splitKarakter[i].equals("")) {
                                kelasCBO.add(splitKarakter[i]);
                            }
                        }
                    }
                }
            }

//            /**
//             * DEBUG
//             */
//            Iterator<String> itcbo4 = kelasCBO.iterator();
//            while (itcbo4.hasNext()) {
//                System.out.println("kelas: " + itcbo4.next());
//            }
//
            counterCBO += kelasCBO.size();//beres itung CBO
//                --------------------------------------------------------------
            /**
             * LCOM
             */
            ListenerLCOM objekLCOM = new ListenerLCOM();
            ParseTreeWalker.DEFAULT.walk(objekLCOM, tree);

            Set<String> namaMethods = objekLCOM.getMethodIsi().keySet();
            ArrayList<String> arrLCOM = new ArrayList<>();
            List<Set<String>> himpunanAtribut = new ArrayList<>();

            for (String s : namaMethods) {
                String isiMethod = objekLCOM.getMethodIsi().get(s);
                HashSet<String> instanceVariableMethod = new HashSet<>();
                String temp = "";
                //selama bukan method kosong
                if (!isiMethod.equals("{}")) {
                    isiMethod = isiMethod.replaceAll("\\{", "");
                    isiMethod = isiMethod.replaceAll("\\}", "");
                    String[] isi = isiMethod.split(";");
                    for (int i = 0; i < isi.length; i++) {
                        String[] cc = isi[i].split("[^a-zA-Z0-9()_'\" ]");
                        for (int k = 0; k < cc.length; k++) {
                            if (cc[k].contains("(") && cc[k].contains(")") && !cc[k].contains("()")) {
                                String[] cd = cc[k].split("\\(");
                                for (int j = 0; j < cd.length; j++) {
                                    cd[j] = cd[j].replaceAll("\\)", "");
                                    if (objekLCOM.getInstanceVariable().get(cd[j]) != null) {
                                        instanceVariableMethod.add(cd[j]);
                                    }
                                }
                            } else {
                                if (objekLCOM.getInstanceVariable().get(cc[k]) != null) {
                                    instanceVariableMethod.add(cc[k]);
                                }
                            }
                        }
                    }
                }

                /**
                 * DEBUG
                 */
//            Iterator<String> it = instanceVariableMethod.iterator();
//            while (it.hasNext()) {
//                System.out.println("instance variable: " + it.next());
//            }
                himpunanAtribut.add(instanceVariableMethod);
            }
            /**
             * DEBUG
             */
//        for (Set<String> himpunanAtribut1 : himpunanAtribut) {
//            System.out.println("himpunanAtribut: " + himpunanAtribut1);
//        }

            ArrayList<String> arrTempLCOM = arrLCOM;
            int q = 0;
            int p = 0;

            for (int i = 0; i < himpunanAtribut.size(); i++) {
                Set<String> temp = new HashSet<>();

                for (int j = 0; j < himpunanAtribut.size(); j++) {
                    Set<String> temp2 = new HashSet<>();

                    if (i != j && i < j) {
                        temp.addAll(himpunanAtribut.get(i));
                        temp2.addAll(himpunanAtribut.get(j));
                        temp.retainAll(temp2);
                        if (temp.iterator().hasNext() == false) {
                            p++;
                        } else {
                            q++;
                        }
                        temp.clear();
                        temp2.clear();
                    }
                }
            }
            if (p < q) {
                counterLCOM += 0;
            } else {
                int selisihpq = p - q;
                counterLCOM += selisihpq;
            } //beres itung LCOM

//            ------------------------------------------------------------------
            /**
             * DIT
             */
            ListenerDIT objekDIT = new ListenerDIT();
            ParseTreeWalker.DEFAULT.walk(objekDIT, tree);

            String namaKelasDIT = objekDIT.getNamaKelas();
            String namaPackageDIT = objekDIT.getNamaPackage();
            String namaLengkap = "";
            if (namaPackageDIT != null) {
                namaLengkap = namaPackageDIT + "." + namaKelasDIT;
            }
            try {
                Class c = Class.forName(namaLengkap);
                if (!c.isInterface()) {
                    c = c.getSuperclass();
                    while (c != null) {
                        counterDIT++;
                        c = c.getSuperclass();
                    }
                }
            } catch (ClassNotFoundException e) {
                System.out.println("KOSONG");
            }
        }
        /**
         * DEBUG
         */
//        System.out.println("nilai DIT: " + counterDIT);
        /**
         *
         */
        /**
         * SOUT2 HASIL
         */
        this.setNilaiNOC(counterNOC);
        this.setNilaiCBO(counterCBO);
        this.setNilaiRFC(counterRFC);
        this.setNilaiLCOM(counterLCOM);
        this.setNilaiDIT(counterDIT);
    }

    public int getNilaiCBO() {
        return nilaiCBO;
    }

    public void setNilaiCBO(int nilaiCBO) {
        this.nilaiCBO = nilaiCBO;
    }

    public int getNilaiDIT() {
        return nilaiDIT;
    }

    public void setNilaiDIT(int nilaiDIT) {
        this.nilaiDIT = nilaiDIT;
    }

    public int getNilaiLCOM() {
        return nilaiLCOM;
    }

    public void setNilaiLCOM(int nilaiLCOM) {
        this.nilaiLCOM = nilaiLCOM;
    }

    public int getNilaiNOC() {
        return nilaiNOC;
    }

    public void setNilaiNOC(int nilaiNOC) {
        this.nilaiNOC = nilaiNOC;
    }

    public int getNilaiRFC() {
        return nilaiRFC;
    }

    public void setNilaiRFC(int nilaiRFC) {
        this.nilaiRFC = nilaiRFC;
    }

    public Set<String> titikHasil(Stack<String> stack, Stack<String> stack2, HashMap<String, String> argumen, HashMap<String, HashMap<String, Set<String>>> namaLocVar, HashMap<String, Set<String>> insVar, HashMap<String, Set<String>> tempInsVar, String kalimat) {
        Set<String> res = new HashSet<>();
        while (!stack.empty()) {
            String pop = stack.pop();
            if (kalimat.contains("this.")) {
                kalimat = kalimat.replace("this.", "");
                String[] splitThisTitik = kalimat.split("\\.");
                if (splitThisTitik.length > 1) {
                    if (insVar.get(splitThisTitik[0]) != null) {
                        Set<String> kelasInsVar = insVar.get(splitThisTitik[0]);
                        res.addAll(kelasInsVar);
                        if (tempInsVar.get(splitThisTitik[0]) != null) {
                            res.addAll(tempInsVar.get(splitThisTitik[0]));
                        }
                    }
                }
            } else {
                String[] splitTitik = kalimat.split("\\.");
                if (splitTitik[0].length() > 0) {
                    Character a = splitTitik[0].charAt(0);
//                    System.out.println("a " + a);
                    if (Character.isUpperCase(a)) {
                        res.add(splitTitik[0]);
                    }
                }
                if (argumen.get(splitTitik[0]) != null) {
                    res.add(argumen.get(splitTitik[0]));
                } else {
                    if (!stack.isEmpty()) {
                        if (namaLocVar.get(stack.peek()) != null) {
                            splitTitik[0] = "[" + splitTitik[0] + "]";
                            String namaLV = namaLocVar.get(stack.peek()).keySet().toString();
                            if (namaLV.equals(splitTitik[0])) {
                                String namaKelasLV = namaLocVar.get(stack.peek()).values().toString();
                                res.add(namaKelasLV);
                            }

                        } else {
                            stack2.push(pop);
                        }
                    }
                }
            }
        }
        return res;
    }
}
