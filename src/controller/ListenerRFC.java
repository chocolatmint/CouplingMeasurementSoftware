package controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import model.grammar.JavaBaseListener;
import model.grammar.JavaParser;

/**
 *
 * @author Maudy
 */
public class ListenerRFC extends JavaBaseListener {

    private ArrayList<String> namaMethod = new ArrayList<>();
    private Set<String> namaMethodInvocation = new HashSet<>();
    private HashMap<String, Boolean> isFirst = new HashMap<>();
    @Override

    public void enterExpression(JavaParser.ExpressionContext ctx) {
        if (!ctx.expression().isEmpty()) {
//           ignore *
//            if ((ctx.getChild(ctx.expression().size()).getText().equals("."))) {
//                this.namaMethodPendek.add(ctx.getChild(ctx.expression().size() + 1).getText());
//            } *

            if ((ctx.getChild(ctx.expression().size()).getText().equals("("))) {
                if (ctx.primary() == null) {
                    this.namaMethodInvocation.add(ctx.getText());
//                    ignore *
//                    this.namaMethodInvocation.add(ctx.getChild(ctx.expression().size()+ 1).getText()); *
                }
            }
        }
    }

    @Override
    public void enterInterfaceMethodDeclaration(JavaParser.InterfaceMethodDeclarationContext ctx) {
        if (!ctx.isEmpty()) {
            String methodName = ctx.Identifier().getText();
            String paramMethod = ctx.Identifier().getText() + ctx.formalParameters().getText();

            //cek apakah dia mengandung > 1 parameter
            if (paramMethod.contains(",")) {
                //cek apakah method udh ada sebelomnya/gak. kalau gak dia dimasukin
                if (isFirst.get(methodName) == null) {
                    this.namaMethod.add(ctx.Identifier().getText());
                    this.isFirst.put(methodName, Boolean.TRUE);
                } else {
                    List<JavaParser.FormalParameterContext> list = ctx.formalParameters().formalParameterList().formalParameter();
                    String temp = "";
                    for (int i = 0; i < list.size(); i++) {
                        if (i == list.size() - 1) {
                            temp += list.get(i).typeType().getText();
                            this.namaMethod.add(ctx.Identifier().getText() + temp);
                            temp = "";
                        } else {
                            temp += list.get(i).typeType().getText();
                        }
                    }
                }
            } else {
                if (isFirst.get(methodName) == (null)) {
                    this.namaMethod.add(ctx.Identifier().getText());
                    this.isFirst.put(methodName, Boolean.TRUE);
                } else {
                    List<JavaParser.FormalParameterContext> list = ctx.formalParameters().formalParameterList().formalParameter();
                    String temp = "";
                    for (int i = 0; i < list.size(); i++) {
                        if (i == list.size() - 1) {
                            temp += list.get(i).typeType().getText();
                            this.namaMethod.add(ctx.Identifier().getText() + temp);
                            temp = "";
                        } else {
                            temp += list.get(i).typeType().getText();
                        }
                    }
                }
            }
        }
    }

    @Override
    public void enterMethodDeclaration(JavaParser.MethodDeclarationContext ctx) {
        if (!ctx.isEmpty()) {
            String methodName = ctx.Identifier().getText();
            String paramMethod = ctx.Identifier().getText() + ctx.formalParameters().getText();

            //cek apakah dia mengandung > 1 parameter
            if (paramMethod.contains(",")) {
                //cek apakah method udh ada sebelomnya/gak. kalau gak dia dimasukin
                if (isFirst.get(methodName) == null) {
                    this.namaMethod.add(ctx.Identifier().getText());
                    this.isFirst.put(methodName, Boolean.TRUE);
                } else {
                    List<JavaParser.FormalParameterContext> list = ctx.formalParameters().formalParameterList().formalParameter();
                    String temp = "";
                    for (int i = 0; i < list.size(); i++) {
                        if (i == list.size() - 1) {
                            temp += list.get(i).typeType().getText();
                            this.namaMethod.add(ctx.Identifier().getText() + temp);
                            temp = "";
                        } else {
                            temp += list.get(i).typeType().getText();
                        }
                    }
                }
            } else {
//                System.out.println(methodName);
                if (isFirst.get(methodName) == (null)) {
                    this.namaMethod.add(ctx.Identifier().getText());
                    this.isFirst.put(methodName, Boolean.TRUE);
                } else {
                    if (ctx.formalParameters().formalParameterList() != null) {
                        List<JavaParser.FormalParameterContext> list = ctx.formalParameters().formalParameterList().formalParameter();
                        String temp = "";
                        for (int i = 0; i < list.size(); i++) {
                            if (i == list.size() - 1) {
                                temp += list.get(i).typeType().getText();
                                this.namaMethod.add(ctx.Identifier().getText() + temp);
                                temp = "";
                            } else {
                                temp += list.get(i).typeType().getText();
                            }
                        }
                    }
                }
            }
        }
    }

    public HashMap<String, Boolean> getIsFirst() {
        return isFirst;
    }

    public void setIsFirst(HashMap<String, Boolean> isFirst) {
        this.isFirst = isFirst;
    }

    public ArrayList<String> getNamaMethod() {
        return namaMethod;
    }

    public void setNamaMethod(ArrayList<String> namaMethod) {
        this.namaMethod = namaMethod;
    }

    public Set<String> getNamaMethodInvocation() {
        return namaMethodInvocation;
    }

    public void setNamaMethodInvocation(Set<String> namaMethodInvocation) {
        this.namaMethodInvocation = namaMethodInvocation;
    }
}
