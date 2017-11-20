package controller;

import model.grammar.JavaBaseListener;
import model.grammar.JavaParser;

import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Maudy
 */
public class ListenerLCOM extends JavaBaseListener {

    private HashMap<String, String> instanceVariable = new HashMap<>();
    private HashMap<String, String> methodIsi = new HashMap<>();

    @Override
    public void enterFieldDeclaration(JavaParser.FieldDeclarationContext ctx) {
        if (!ctx.isEmpty()) {
            this.instanceVariable.put(ctx.variableDeclarators().getText(), ctx.variableDeclarators().getText());
            System.out.println("instance variable: "+ctx.variableDeclarators().getText());
        }
    }

    @Override
    public void enterMethodDeclaration(JavaParser.MethodDeclarationContext ctx) {
        if (!ctx.isEmpty()) {
            String namaMethod = ctx.Identifier().getText() + ctx.formalParameters().getText();
            String isiMethod = ctx.methodBody().getText();
            List<JavaParser.BlockStatementContext> list = ctx.methodBody().block().blockStatement();
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).statement() != null && list.get(i).statement().getChildCount() > 2) {
                    if (list.get(i).statement().getChild(0).getText().equals("return")) {
                        String a = list.get(i).statement().getChild(0).getText() + list.get(i).statement().getChild(1).getText();
                        if (isiMethod.contains(a)) {
                            isiMethod = isiMethod.replace(a, list.get(i).statement().getChild(1).getText());
                        }
                    }
                }
            }
            this.methodIsi.put(namaMethod, isiMethod);
            System.out.println("nama method: "+namaMethod);
            System.out.println("isi method: "+isiMethod);
        }
    }

    public HashMap<String, String> getInstanceVariable() {
        return instanceVariable;
    }

    public void setInstanceVariable(HashMap<String, String> instanceVariable) {
        this.instanceVariable = instanceVariable;
    }

    public HashMap<String, String> getMethodIsi() {
        return methodIsi;
    }

    public void setMethodIsi(HashMap<String, String> namaMethod) {
        this.methodIsi = namaMethod;
    }
}
