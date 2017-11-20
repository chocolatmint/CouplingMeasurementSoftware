package controller;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import model.grammar.JavaBaseListener;
import model.grammar.JavaParser;

/**
 * Kelas yang berfungsi untuk me-listen token-token yang nantinya digunakan
 * untuk mengukur nilai CBO
 *
 * @author Maudy
 */
public class ListenerCBO extends JavaBaseListener {

    /*
     Instance variable untuk menyimpan seluruh instance variable bertipe kelas yang dimiliki oleh kelas yang diobservasi
     */
    private HashMap<String, Set<String>> listInstanceVariable = new HashMap<>();
    /*
     Instance variable untuk menyimpan seluruh literal yang dimiliki oleh kelas yang diobservasi
     */
    private Set<String> listLiteral = new HashSet<>();
    /*
     Instance variable untuk menyimpan parameter bertipe kelas yang dimiliki oleh method kelas yang diobservasi
     */
    private HashMap<String, HashMap<String, String>> listMethodParamType = new HashMap<>();
    /*
     Instance variable untuk menyimpan nama method beserta isinya yang dimiliki oleh kelas yang diobservasi
     */
    private HashMap<String, String> methodIsi = new HashMap<>();
    /*
     Instance variable untuk menyimpan nama kelas yang sedang diobservasi
     */
    private String namaKelas = "";
    /*
     Instance variable untuk menyimpan local variable yang dimiliki oleh method kelas yang diobservasi
     */
    private HashMap<String, HashMap<String, Set<String>>> namaTipeLocalVar = new HashMap<>();

    /**
     * Method untuk memasukkan nama kelas milik instance variable kelas yang
     * diobservasi (jika diinstansiasi di dalam konstruktor)
     *
     * @param ctx sebagai akar untuk me-listen token dari kelas yang diobservasi
     */
    @Override
    public void enterConstructorDeclaration(JavaParser.ConstructorDeclarationContext ctx) {
        //jika terdapat konstruktor, maka lakukan:
        if (!ctx.isEmpty()) {
            String className = "";
            String namaInsVar = "";
            //buat list untuk dapetin isi dari konstruktor
            List<JavaParser.BlockStatementContext> list = ctx.constructorBody().block().blockStatement();
            for (int i = 0; i < list.size(); i++) {
                //cek apakah statement kosong? statement masih buat cek isi konstruktor
                if (list.get(i).statement() != null) {
                    //jika tidak buat list baru lagi untuk dapetin instansiasi dari instance variable
                    List<JavaParser.ExpressionContext> list2 = list.get(i).statement().statementExpression().expression().expression();
                    for (int j = 0; j < list2.size(); j++) {
                        //cek apakah creator kosong? 
                        //creator buat ambil nama kelas di sebelah keyword new di deklarasi instansiasi instance variable
                        if (list2.get(j).creator() != null) {
                            //masukin tipe kelas yang dipake pada saat instansiasi instance variable di konstruktor
                            className = list2.get(j).creator().getText();
//                            System.out.println("1. tipe instance variable: " + className);
                        } else {
                            //masukin nama instance variable yang diinstansiasi di konstruktor
                            namaInsVar = list2.get(j).getText();
//                            System.out.println("1. nama instance variable: " + namaInsVar);
                        }
                    }
                    //cek apakah insVar kosong? sama cek si insvar ada this. ga? kalo ngga gak usah dianggep karena asumsi manggil instance variable harus pake this.
                    if (!namaInsVar.equals("") && namaInsVar.contains("this.")) {
                        //remove this. dari namaInsVar
                        namaInsVar = namaInsVar.replaceAll("this.", "");
                        //cek apakah instance variable udah pernah dimasukin ke listInstanceVariable.
                        //berguna kalo misalnya ada konstruktor > 1 dan instansiasi dengan jenis kelas yang berbeda
                        if (this.listInstanceVariable.get(namaInsVar) != null) {
                            //masukin nama kelas yang lama ke tampKelas
                            Set<String> tampKelas = this.listInstanceVariable.get(namaInsVar);
                            //masukin kelas baru ke tampKelas
                            tampKelas.add(className);
                            //masukin tampKelas ke listInstanceVariable
                            //jadi, kayak ngeremove tapi langsung masukin lagi
                            this.listInstanceVariable.put(namaInsVar, tampKelas);
                        } else if (this.listInstanceVariable.get(namaInsVar) == null) {
                            //buat hashset baru buat tampungan nama kelas
                            Set<String> temp = new HashSet<>();
                            temp.add(className);
                            //masukin temp ke listInstanceVariable
                            this.listInstanceVariable.put(namaInsVar, temp);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void enterEnhancedForControl(JavaParser.EnhancedForControlContext ctx) {
        if (!ctx.isEmpty()) {
            String namaKls = ctx.typeType().classOrInterfaceType().getText();
            String namaForVar = ctx.variableDeclaratorId().getText();
            Set<String> tempKelasForEach = new HashSet<>();
            HashMap<String, Set<String>> tempForEach = new HashMap<>();
            tempKelasForEach.add(namaKls);
            tempForEach.put(namaForVar, tempKelasForEach);
            this.namaTipeLocalVar.put(namaKls + namaForVar, tempForEach);
//            System.out.println("2. Kelas foreach: " + namaKls);
//            System.out.println("2. insvar foreach: " + namaForVar);
        }
    }

    /**
     * Method untuk memasukkan nama kelas milik instance variable kelas yang
     * diobservasi (sekaligus masukin kelas jika terdapat instansiasi di sini)
     *
     * @param ctx sebagai akar untuk me-listen token dari kelas yang diobservasi
     */
    @Override
    public void enterFieldDeclaration(JavaParser.FieldDeclarationContext ctx) {
        Set kelasLain = new HashSet<>();
        //jika terdapat deklarasi instance variabel yang bertipe kelas, maka lakukan:
        if (ctx.typeType().classOrInterfaceType() != null) {
            //buat list untuk dapetin deklarasi instance variable
            List<JavaParser.VariableDeclaratorContext> list = ctx.variableDeclarators().variableDeclarator();
            String namaInstanceVariable = "";
            for (int i = 0; i < list.size(); i++) {
                //dapetin nama instance variable
                namaInstanceVariable = list.get(i).variableDeclaratorId().getText();
//                System.out.println("4. nama instance variable: " + namaInstanceVariable);
                //cek apakah dia ada deklarasi tipe dari instance variablenya
                if (list.get(i).variableInitializer() != null && list.get(i).variableInitializer().expression().creator() != null) {
                    //creator setelah keyword new, mengindikasikan nama tipe kelasnya
                    String namaKelasGeneric = list.get(i).variableInitializer().expression().creator().getText();
                    kelasLain.add(namaKelasGeneric);
//                    System.out.println("3. nama kelas generic setelah keyword new: " + namaKelasGeneric);
                }
            }
            //cek apakah ada generic type di sebelah kiri = deklarasi instance variable
            if (ctx.typeType().classOrInterfaceType().typeArguments() != null) {
                String namaKelasGeneric = ctx.typeType().classOrInterfaceType().getText();
//                System.out.println("3. nama kelas generic: " + namaKelasGeneric);
                kelasLain.add(namaKelasGeneric);
            } else {
                //kalau bukan langsung masukin nama kelas yang sendiri
                String namaKls = ctx.typeType().classOrInterfaceType().getText();
//                System.out.println("3. nama kelas bukan generic: " + namaKls);
                kelasLain.add(namaKls);
            }
            //cek apakah namaInstanceVariable pernah dimasukin ke dalem listInstanceVariable
            if (this.listInstanceVariable.get(namaInstanceVariable) != null) {
                //kalau iya, masukin list kelas lama ke tampungan
                Set<String> tampKelas = this.listInstanceVariable.get(namaInstanceVariable);
                //masukin list kelas baru ke tampungan
                tampKelas.addAll(kelasLain);
                //ganti value dengan tampungan jadi mengandung kelas lama dan baru
                this.listInstanceVariable.put(namaInstanceVariable, tampKelas);
            } else {
                //kalau ngga buat kamus barunya
                this.listInstanceVariable.put(namaInstanceVariable, kelasLain);
            }
        }
    }

    /**
     * Method untuk memasukkan semua literal yang dimiliki oleh kelas yang
     * diobservasi Nantinya listLiteral dipakai untuk ngereplace semua literal
     * jadi String kosong
     *
     * @param ctx sebagai akar untuk me-listen token dari kelas yang diobservasi
     */
    @Override
    public void enterLiteral(JavaParser.LiteralContext ctx) {
        //jika terdapat deklarasi literal, maka lakukan:
        if (!ctx.isEmpty()) {
            //cek apakah literal merupakan string (pake ""/'')
            if (ctx.getText().startsWith("\"") && ctx.getText().endsWith("\"") || ctx.getText().startsWith("\'") && ctx.getText().endsWith("\'")) {
                this.listLiteral.add(ctx.getText());
            }
//            System.out.println("4. literal: " + ctx.getText());
        }
    }

    /**
     * Method untuk memasukkan local variable yang dimiliki oleh kelas yang
     * diobservasi (yang bertipe kelas)
     *
     * @param ctx sebagai akar untuk me-listen token dari kelas yang diobservasi
     */
    @Override
    public void enterLocalVariableDeclaration(JavaParser.LocalVariableDeclarationContext ctx) {
        //jika terdapat local variable bertipe kelas, maka lakukan:
        if (ctx.typeType().classOrInterfaceType() != null) {
            HashMap<String, Set<String>> pasanganLocalVar = new HashMap<>();
            Set<String> kelasGenerics = new HashSet<>();
            String namalocVar = "";
            //buat list untuk ambil deklarasi local variable (selain tipe kelas di awal)
            List<JavaParser.VariableDeclaratorContext> list = ctx.variableDeclarators().variableDeclarator();
            for (int i = 0; i < list.size(); i++) {
                //ambil nama local variable
                namalocVar = list.get(i).variableDeclaratorId().getText();
                //cek apakah dia ada deklarasi tipe dari instance variablenya
                if (list.get(i).variableInitializer() != null) {
                    if (list.get(i).variableInitializer().expression() != null) {
                        if (list.get(i).variableInitializer().expression().creator() != null) {
                            //creator setelah keyword new, mengindikasikan nama tipe kelasnya
                            kelasGenerics.add(list.get(i).variableInitializer().expression().creator().getText());
                            //masukin nama local variable dan tipenya ke temp
                            pasanganLocalVar.put(namalocVar, kelasGenerics);
//                    System.out.println("5. nama kelas generic setelah keyword new: " + kelasGenerics);
                        }
                    }
                }
            }
            //cek apakah ada generic type di sebelah kiri = deklarasi instance variable
            if (ctx.typeType().classOrInterfaceType().typeArguments() != null) {
                kelasGenerics.add(ctx.typeType().classOrInterfaceType().getText());
//                System.out.println("5. nama kelas generic: " + kelasGenerics);
                pasanganLocalVar.put(namalocVar, kelasGenerics);
            } else {
                //kalau bukan langsung masukin nama kelas yang sendiri
                HashSet<String> namaKls = new HashSet<>();
                namaKls.add(ctx.typeType().classOrInterfaceType().getText());
//                System.out.println("5. nama kelas bukan generic: " + namaKls);
                pasanganLocalVar.put(namalocVar, namaKls);
            }
            //masukin deklarasi local variable yang lengkap dan local variable (nama local variable+jenis kelasnya) yang dipunya ke dalem namaTipeLocalVar
            String deklarasi = ctx.getText();
            if (ctx.getText().contains("\"") || (ctx.getText().contains("\'"))) {
                deklarasi = deklarasi.replaceAll("(\"[^\"\n]+\")", "");
                deklarasi = deklarasi.replaceAll("(\"[^\"\n]+\")", "");
            }
            this.namaTipeLocalVar.put(deklarasi, pasanganLocalVar);
        }
    }

    /**
     * Method untuk memasukkan: 1. Param dari suatu method (nama method+tipe
     * paramnya) 2. Isi method (yang udah gak ada returnnya) dari sebuah kelas
     * yang diobservasi
     *
     * @param ctx sebagai akar untuk me-listen token dari kelas yang diobservasi
     */
    @Override
    public void enterMethodDeclaration(JavaParser.MethodDeclarationContext ctx) {
        //jika terdapat deklarasi method, maka lakukan:
        if (!ctx.isEmpty()) {
            //masukin nama methodnya
            String namaMethod = ctx.Identifier().getText() + ctx.formalParameters().getText();
            HashMap<String, String> pasanganParam = new HashMap<>();
//            System.out.println("6. nama method: " + namaMethod);
            //cek apakah terdapat parameter bertipe kelas di deklarasi method
            if (ctx.formalParameters().formalParameterList() != null) {
                String namaParam = "";
                //buat list untuk ambil nama param dan tipe kelas param tersebut
                List<JavaParser.FormalParameterContext> list = ctx.formalParameters().formalParameterList().formalParameter();
                for (int i = 0; i < list.size(); i++) {
                    //masukin nama param
                    namaParam = list.get(i).variableDeclaratorId().getText();
                    //cek apakah param merupakan tipe kelas
                    if (list.get(i).typeType().classOrInterfaceType() != null) {
                        String tipeKelasParam = list.get(i).typeType().classOrInterfaceType().getText();
                        //masukin ke temp sementara (nama param dan tipe kelasnya)
                        //namaParam dan tipeKelasParam harus ada isinya
                        if (namaParam != null && tipeKelasParam != null) {
                            pasanganParam.put(namaParam, tipeKelasParam);
//                        System.out.println("6. tipe kelas param: " + tipeKelasParam);
                        }
                    }
                }
                //masukin ke listMethodParamType (nama method, nama param, dan tipe kelas)
                //cek apakah pasangan param kosong
                if (!pasanganParam.isEmpty()) {
                    //kalo pasangan param kosong mah buat apa dimasukin ke sini
                    this.listMethodParamType.put(namaMethod, pasanganParam);
                }
            }
            String isiMethod = "";
            //buat list untuk ambil isi dari seluruh method
            List<JavaParser.BlockStatementContext> list = ctx.methodBody().block().blockStatement();
            for (int i = 0; i < list.size(); i++) {
                //cek apakah isiMethod masih kosong? 
                //kalo iya (dan si method pasti punya isi) maka tambahin "{" di depan isi method
                if (isiMethod.length() == 0) {
                    isiMethod = "{";
                }
                isiMethod = isiMethod + list.get(i).getText();
//                System.out.println("6. isi method: " + isiMethod);

                if (list.get(i).statement() != null && list.get(i).statement().catchClause() != null) {
                    List<JavaParser.CatchClauseContext> list2 = list.get(i).statement().catchClause();
                    for (int j = 0; j < list2.size(); j++) {
                        List<JavaParser.QualifiedNameContext> list3 = list2.get(j).catchType().qualifiedName();
                        Set<String> tempKelasExc = new HashSet<>();
                        HashMap<String, Set<String>> tempException = new HashMap<>();
                        String namaKelasException = list3.get(j).getText();
                        String namaInsVarException = list2.get(j).Identifier().getText();
                        tempKelasExc.add(namaKelasException);
                        tempException.put(namaInsVarException, tempKelasExc);
                        this.namaTipeLocalVar.put(namaKelasException + namaInsVarException, tempException);
//                        System.out.println("6. Kelas catch: " + namaKelasException);
//                        System.out.println("6. catch clause: " + namaInsVarException);
                    }
                }
                if (list.get(i).statement() != null && list.get(i).statement().forControl() != null) {

                }
                //cek apakah isi mengandung kata return
                //dilakukan karena antlr ignore whitespace, jadi return bisa ngubah nama instance variable
                //sebenernya cuma ngecek si statement punya statementExpression dan ";" gak?
                if (list.get(i).statement() != null && list.get(i).statement().getChildCount() > 2) {
                    //cek kalo ada kata returnnya (yang selalu harus ditaro di paling awal)
                    if (list.get(i).statement().getChild(0).getText().equals("return")) {
                        //masukin ke tempPakeReturn untuk ambil seluruh statement
                        String tempPakeReturn = list.get(i).statement().getText();
//                        System.out.println("6. return: " + tempPakeReturn);
                        //cek apakah ada isiMethod yang mengandung tempPakeReturn
                        if (isiMethod.contains(tempPakeReturn)) {
                            //kalo iya ganti si tempPakeReturn itu dengan child pertamanya aja (nama variabel)
                            isiMethod = isiMethod.replace(tempPakeReturn, list.get(i).statement().getChild(1).getText());
                            //tambahin ";" karena getChild(1) cuma ambil variable aja
                            isiMethod += ";";
                        }
                    }
                }
            }
            //tutup isiMethod pake "}"
            isiMethod = isiMethod + "}";
            //masukin namaMethod dan isiMethod ke methodIsi
            this.methodIsi.put(namaMethod, isiMethod);
//            System.out.println("6. nama method final: " + namaMethod);
//            System.out.println("6. isi method final: " + isiMethod);
        }
    }

    /**
     * Method untuk ambil listInstanceVariable
     *
     * @return listInstanceVariable
     */
    public HashMap<String, Set<String>> getListInstanceVariable() {
        return this.listInstanceVariable;
    }

    /**
     * Method untuk set listInstanceVariable
     *
     * @param listInstanceVariable
     */
    public void setListInstanceVariable(HashMap<String, Set<String>> listInstanceVariable) {
        this.listInstanceVariable = listInstanceVariable;
    }

    /**
     * Method untuk ambil listLiteral
     *
     * @return listLiteral
     */
    public Set<String> getListLiteral() {
        return this.listLiteral;
    }

    /**
     * Method untuk set listLiteral
     *
     * @param listLiteral
     */
    public void setListLiteral(Set<String> listLiteral) {
        this.listLiteral = listLiteral;
    }

    /**
     * Method untuk ambil listMethodParamType
     *
     * @return listMethodParamType
     */
    public HashMap<String, HashMap<String, String>> getListMethodParamType() {
        return this.listMethodParamType;
    }

    /**
     * Method untuk set listMethodParamType
     *
     * @param listMethodParamType
     */
    public void setListMethodParamType(HashMap<String, HashMap<String, String>> listMethodParamType) {
        this.listMethodParamType = listMethodParamType;
    }

    /**
     * Method untuk ambil methodIsi
     *
     * @return methodIsi
     */
    public HashMap<String, String> getMethodIsi() {
        return this.methodIsi;
    }

    /**
     * Method untuk set methodIsi
     *
     * @param methodIsi
     */
    public void setMethodIsi(HashMap<String, String> methodIsi) {
        this.methodIsi = methodIsi;
    }

    /**
     * Method untuk ambil namaKelas
     *
     * @return namaKelas
     */
    public String getNamaKelas() {
        return this.namaKelas;
    }

    /**
     * Method untuk set namaKelas
     *
     * @param namaKelas
     */
    public void setNamaKelas(String namaKelas) {
        this.namaKelas = namaKelas;
    }

    /**
     * Method untuk ambil namaTipeLocalVar
     *
     * @return namaTipeLocalVar
     */
    public HashMap<String, HashMap<String, Set<String>>> getNamaTipeLocalVar() {
        return this.namaTipeLocalVar;
    }

    /**
     * Method untuk set namaTipeLocalVar
     *
     * @param namaTipeLocalVar
     */
    public void setNamaTipeLocalVar(HashMap<String, HashMap<String, Set<String>>> namaTipeLocalVar) {
        this.namaTipeLocalVar = namaTipeLocalVar;
    }
}
