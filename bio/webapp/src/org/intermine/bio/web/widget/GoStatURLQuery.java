package org.intermine.bio.web.widget;

/*
 * Copyright (C) 2002-2007 FlyMine
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  See the LICENSE file for more
 * information or http://www.gnu.org/copyleft/lesser.html.
 *
 */

import java.util.ArrayList;
import java.util.List;

import org.intermine.objectstore.query.ConstraintOp;

import org.intermine.metadata.Model;
import org.intermine.objectstore.ObjectStore;
import org.intermine.path.Path;
import org.intermine.web.logic.bag.InterMineBag;
import org.intermine.web.logic.query.Constraint;
import org.intermine.web.logic.query.MainHelper;
import org.intermine.web.logic.query.PathNode;
import org.intermine.web.logic.query.PathQuery;
import org.intermine.web.logic.widget.EnrichmentWidgetURLQuery;


/**
 * Builds a query to get all the genes (in bag) associated with specified go term.
 * @author Julie Sullivan
 */
public class GoStatURLQuery implements EnrichmentWidgetURLQuery
{
    ObjectStore os;
    InterMineBag bag;
    String key;
    /**
     * @param os
     * @param key
     * @param bag
     */
     public GoStatURLQuery(ObjectStore os, InterMineBag bag, String key) {
         this.bag = bag;
         this.key = key;
         this.os = os;
     }

    /**
     * @return Query a query to generate the results needed
     */
     public PathQuery generatePathQuery() {

         Model model = os.getModel();
         PathQuery q = new PathQuery(model);

         List<Path> view = new ArrayList<Path>();

         Path geneIdentifier = null;
         Path geneDbId =  null;
         Path geneName =  null;
         Path organismName =  null;
         Path goId = null;
         Path goName = null;
         Path actualGoName = null;
         Path actualGoId = null;

         if (bag.getType().toLowerCase().equals("protein")) {

             geneIdentifier = MainHelper.makePath(model, q, "Protein.gene.identifier");
             geneDbId = MainHelper.makePath(model, q, "Protein.gene.organismDbId");
             geneName = MainHelper.makePath(model, q, "Protein.gene.name");
             organismName = MainHelper.makePath(model, q, "Protein.gene.organism.name");
             goId = MainHelper.makePath(model, q, "Protein.gene.allGoAnnotation.identifier");
             goName = MainHelper.makePath(model, q, "Protein.gene.allGoAnnotation.name");
             actualGoName = MainHelper.makePath(
                            model, q, "Protein.gene.allGoAnnotation.actualGoTerms.name");
             actualGoId = MainHelper.makePath(
                          model, q, "Protein.gene.allGoAnnotation.actualGoTerms.identifier");

             view.add(MainHelper.makePath(model, q, "Protein.identifier"));
             view.add(MainHelper.makePath(model, q, "Protein.primaryAccession"));

         } else {

             geneIdentifier = MainHelper.makePath(model, q, "Gene.identifier");
             geneDbId = MainHelper.makePath(model, q, "Gene.organismDbId");
             geneName = MainHelper.makePath(model, q, "Gene.name");
             organismName = MainHelper.makePath(model, q, "Gene.organism.name");
             goId = MainHelper.makePath(model, q, "Gene.allGoAnnotation.identifier");
             goName = MainHelper.makePath(model, q, "Gene.allGoAnnotation.name");
             actualGoName = MainHelper.makePath(model,
                                                q, "Gene.allGoAnnotation.actualGoTerms.name");
             actualGoId = MainHelper.makePath(model,
                                              q, "Gene.allGoAnnotation.actualGoTerms.identifier");
         }

         view.add(geneIdentifier);
         view.add(geneDbId);
         view.add(geneName);
         view.add(organismName);
         view.add(goId);
         view.add(goName);
         view.add(actualGoName);
         view.add(actualGoId);

         q.setView(view);

         String bagType = bag.getType();
         ConstraintOp constraintOp = ConstraintOp.IN;
         String constraintValue = bag.getName();
         String label = null, id = null, code = q.getUnusedConstraintCode();
         Constraint c = new Constraint(constraintOp, constraintValue, false, label, code, id, null);
         q.addNode(bagType).getConstraints().add(c);

         // can't be a NOT relationship!
         constraintOp = ConstraintOp.IS_NULL;
         code = q.getUnusedConstraintCode();

         PathNode qualifierNode = null;
         if (bag.getType().toLowerCase().equals("protein")) {
             qualifierNode = q.addNode("Protein.gene.allGoAnnotation.qualifier");
         } else {
             qualifierNode = q.addNode("Gene.allGoAnnotation.qualifier");
         }
         Constraint qualifierConstraint
         = new Constraint(constraintOp, null, false, label, code, id, null);
         qualifierNode.getConstraints().add(qualifierConstraint);

         // go term
         constraintOp = ConstraintOp.EQUALS;
         code = q.getUnusedConstraintCode();
         PathNode goTermNode = q.addNode("Gene.allGoAnnotation.identifier");
         if (bag.getType().toLowerCase().equals("protein")) {
             goTermNode = q.addNode("Protein.gene.allGoAnnotation.identifier");
         } else {
             goTermNode  = q.addNode("Gene.allGoAnnotation.identifier");
         }
         Constraint goTermConstraint
                         = new Constraint(constraintOp, key, false, label, code, id, null);
         goTermNode.getConstraints().add(goTermConstraint);

         q.setConstraintLogic("A and B and C");
         q.syncLogicExpression("and");

        return q;
    }
}
