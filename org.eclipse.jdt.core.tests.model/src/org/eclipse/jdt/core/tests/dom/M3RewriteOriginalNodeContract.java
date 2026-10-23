// SPDX-License-Identifier: Apache-2.0
// SPDX-FileCopyrightText: 2026 Hitesh Soliwal and Contributors to the Synexia Project
package org.eclipse.jdt.core.tests.dom;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

/** Standalone real-JDT core contract; no M3 product or test-agent dependency. */
public final class M3RewriteOriginalNodeContract {
    private static int checks;
    private M3RewriteOriginalNodeContract() { }
    private static void check(boolean accepted, String message) {
        checks++;
        if (!accepted) throw new AssertionError(message);
    }
    private static void refuse(Runnable operation, String message) {
        try { operation.run(); throw new AssertionError("admitted " + message); }
        catch (IllegalArgumentException expected) { checks++; }
    }
    public static void main(String[] args) {
        ASTParser parser = ASTParser.newParser(AST.JLS21);
        parser.setSource("class X { void m(){ int x=1; x++; } }".toCharArray());
        CompilationUnit tree = (CompilationUnit) parser.createAST(null);
        AST ast = tree.getAST();
        Block body = ((TypeDeclaration) tree.types().get(0)).getMethods()[0].getBody();
        ASTNode first = (ASTNode) body.statements().get(0);
        ASTNode second = (ASTNode) body.statements().get(1);
        ASTRewrite rewrite = ASTRewrite.create(ast);
        ASTNode copy = rewrite.createCopyTarget(first);
        ASTNode move = rewrite.createMoveTarget(second);
        ASTNode created = ast.newEmptyStatement();
        long generation = ast.modificationCount();
        check(rewrite.getOriginalNode(first) == first, "ordinary original identity");
        check(rewrite.getOriginalNode(created) == created, "new semantic node identity");
        check(rewrite.getOriginalNode(copy) == first, "copy resolves the existing original");
        check(rewrite.getOriginalNode(move) == second, "move resolves the existing original");
        check(ast.modificationCount() == generation, "lookup does not modify AST generation");
        check(first.getParent() == body && second.getParent() == body && body.statements().size() == 2,
                "lookup does not change physical parents");
        ListRewrite planned = rewrite.getListRewrite(body, Block.STATEMENTS_PROPERTY);
        planned.remove(second, null);
        planned.insertFirst(move, null);
        planned.insertLast(copy, null);
        check(planned.getRewrittenList().size() == 3, "original rewrite-list contract preserved");
        check(rewrite.getOriginalNode((ASTNode) planned.getRewrittenList().get(0)) == second,
                "rewritten ordering resolves actual move source");
        check(rewrite.getOriginalNode((ASTNode) planned.getRewrittenList().get(2)) == first,
                "rewritten ordering resolves actual copy source");
        refuse(() -> rewrite.getOriginalNode(null), "null node");
        AST other = AST.newAST(AST.JLS21, false);
        refuse(() -> rewrite.getOriginalNode(other.newEmptyStatement()), "foreign AST node");
        ASTNode raw = rewrite.createStringPlaceholder("return;", ASTNode.RETURN_STATEMENT);
        refuse(() -> rewrite.getOriginalNode(raw), "raw text as a semantic node");
        ASTNode group = rewrite.createGroupNode(new ASTNode[] { ast.newEmptyStatement(), ast.newEmptyStatement() });
        refuse(() -> rewrite.getOriginalNode(group), "group as one semantic node");
        ASTRewrite rangeRewrite = ASTRewrite.create(ast);
        ASTNode range = rangeRewrite.getListRewrite(body, Block.STATEMENTS_PROPERTY).createCopyTarget(first, second);
        refuse(() -> rangeRewrite.getOriginalNode(range), "range as one semantic node");
        check(first.getStartPosition() >= 0 && copy.getStartPosition() == -1,
                "lookup does not overwrite original or placeholder source ranges");
        System.out.println("M3_JDT_ORIGINAL_NODE_PASS checks=" + checks);
    }
}
