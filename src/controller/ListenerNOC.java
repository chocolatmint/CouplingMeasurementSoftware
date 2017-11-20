package controller;

import model.grammar.JavaBaseListener;
import model.grammar.JavaParser;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Maudy
 */
public class ListenerNOC extends JavaBaseListener {

    private String superclassName;

    /*
    * Method untuk memasukkan nama kelas 
    */
    @Override
    public void enterClassDeclaration(JavaParser.ClassDeclarationContext ctx) {
        if (ctx.typeType() != null) {
            if (ctx.typeType().classOrInterfaceType() != null) {
                this.superclassName = ctx.typeType().classOrInterfaceType().getText();
            }
        }
    }

    public String getSuperclassName() {
        return this.superclassName;
    }

    public void setSuperclassName(String superclassName) {
        this.superclassName = superclassName;
    }

}
