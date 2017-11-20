/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import model.grammar.JavaBaseListener;
import model.grammar.JavaParser;

/**
 *
 * @author Maudy
 */
public class ListenerDIT extends JavaBaseListener {

    private String namaPackage;
    private String namaKelas;

    @Override
    public void enterPackageDeclaration(JavaParser.PackageDeclarationContext ctx) {
        if (!ctx.isEmpty()) {
            this.namaPackage = ctx.qualifiedName().getText();
            System.out.println("nama package "+namaPackage);
        }
    }

    @Override
    public void enterClassDeclaration(JavaParser.ClassDeclarationContext ctx) {
        if (!ctx.isEmpty()) {
            this.namaKelas = ctx.Identifier().getText();
            System.out.println("namaKelas "+namaKelas);
        }
    }

    public String getNamaPackage() {
        return namaPackage;
    }

    public void setNamaPackage(String namaPackage) {
        this.namaPackage = namaPackage;
    }

    public String getNamaKelas() {
        return namaKelas;
    }

    public void setNamaKelas(String namaKelas) {
        this.namaKelas = namaKelas;
    }
}
