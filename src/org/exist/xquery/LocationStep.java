/*L
 * Copyright Oracle Inc
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/cadsr-cgmdr-nci-uk/LICENSE.txt for details.
 */

/*
 * eXist Open Source Native XML Database
 * Copyright (C) 2001-2007 The eXist Project
 * http://exist-db.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *  
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *  
 *  $Id: LocationStep.java 7755 2008-05-13 10:11:48Z wolfgang_m $
 */
package org.exist.xquery;

import org.exist.dom.DocumentImpl;
import org.exist.dom.DocumentSet;
import org.exist.dom.ExtArrayNodeSet;
import org.exist.dom.NodeProxy;
import org.exist.dom.NodeSet;
import org.exist.dom.NodeVisitor;
import org.exist.dom.StoredNode;
import org.exist.dom.VirtualNodeSet;
import org.exist.numbering.NodeId;
import org.exist.storage.ElementIndex;
import org.exist.storage.ElementValue;
import org.exist.storage.UpdateListener;
import org.exist.xquery.value.Item;
import org.exist.xquery.value.Sequence;
import org.exist.xquery.value.Type;
import org.w3c.dom.Node;

import java.util.Iterator;

/**
 * Processes all location path steps (like descendant::*, ancestor::XXX).
 * 
 * The results of the first evaluation of the expression are cached for the
 * lifetime of the object and only reloaded if the context sequence (as passed
 * to the {@link #eval(Sequence, Item)} method) has changed.
 * 
 * @author wolf
 */
public class LocationStep extends Step {

    private final int ATTR_DIRECT_SELECT_THRESHOLD = 10;

    protected NodeSet currentSet = null;

    protected DocumentSet currentDocs = null;

    protected UpdateListener listener = null;

    protected Expression parent = null;

    // Fields for caching the last result
    protected CachedResult cached = null;

    protected int parentDeps = Dependency.UNKNOWN_DEPENDENCY;

    protected boolean preload = false;

    protected boolean optimized = false;

    protected boolean inUpdate = false;

    protected boolean useDirectAttrSelect = true;

    protected boolean useDirectChildSelect = false;
    
    // Cache for the current NodeTest type
    private Integer nodeTestType = null;

    /**
     * Creates a new <code>LocationStep</code> instance.
     *
     * @param context a <code>XQueryContext</code> value
     * @param axis an <code>int</code> value
     */
    public LocationStep(XQueryContext context, int axis) {
        super(context, axis);
    }

    /**
     * Creates a new <code>LocationStep</code> instance.
     *
     * @param context a <code>XQueryContext</code> value
     * @param axis an <code>int</code> value
     * @param test a <code>NodeTest</code> value
     */
    public LocationStep(XQueryContext context, int axis, NodeTest test) {
        super(context, axis, test);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.exist.xquery.AbstractExpression#getDependencies()
     */
    public int getDependencies() {
        int deps = Dependency.CONTEXT_SET;
        
        //self axis has an obvious dependency on the context item
        //TODO : I guess every other axis too... so we might consider using Constants.UNKNOWN_AXIS here
        //BUT
        //in a predicate, the expression can't depend on... itself
        if (!this.inPredicate && this.axis == Constants.SELF_AXIS)
        	deps = deps | Dependency.CONTEXT_ITEM; 
        
        //TODO : normally, we should call this one...
        //int deps = super.getDependencies(); ???
        for (Iterator i = predicates.iterator(); i.hasNext();) {
            deps |= ((Predicate) i.next()).getDependencies();
        }
        
        //TODO : should we remove the CONTEXT_ITEM dependency returned by the predicates ? See the comment above.
        //consider nested predicates however...
        
        return deps;
    }

    /**
     * If the current path expression depends on local variables from a for
     * expression, we can optimize by preloading entire element or attribute
     * sets.
     * 
     * @return Whether or not we can optimize 
     */
    protected boolean preloadNodeSets() {
        // TODO : log elsewhere ?
        if (preload) {
            context.getProfiler().message(this, Profiler.OPTIMIZATIONS, null,
                                          "Preloaded NodeSets");
            return true;
        }
        if (inUpdate)
            return false;
        if ((parentDeps & Dependency.LOCAL_VARS) == Dependency.LOCAL_VARS) {
            context.getProfiler().message(this, Profiler.OPTIMIZATIONS, null,
                                          "Preloaded NodeSets");
            return true;
        }
        return false;
    }

    /**
     * The method <code>setPreloadNodeSets</code>
     *
     * @param doPreload a <code>boolean</code> value
     */
    public void setPreloadNodeSets(boolean doPreload) {
        this.preload = doPreload;
    }

    /**
     * The method <code>setPreloadedData</code>
     *
     * @param docs a <code>DocumentSet</code> value
     * @param nodes a <code>NodeSet</code> value
     */
    public void setPreloadedData(DocumentSet docs, NodeSet nodes) {
        this.currentDocs = docs;
        this.currentSet = nodes;
        this.optimized = true;
    }

    /**
     * The method <code>applyPredicate</code>
     *
     * @param outerSequence a <code>Sequence</code> value
     * @param contextSequence a <code>Sequence</code> value
     * @return a <code>Sequence</code> value
     * @exception XPathException if an error occurs
     */
    protected Sequence applyPredicate(Sequence outerSequence,
                                      Sequence contextSequence) throws XPathException {
        if (contextSequence == null)
            return Sequence.EMPTY_SEQUENCE;
        if (predicates.size() == 0)
            // Nothing to apply
            return contextSequence;
        Predicate pred;
        Sequence result = contextSequence;
        for (Iterator i = predicates.iterator(); i.hasNext();) {
            // TODO : log and/or profile ?
            pred = (Predicate) i.next();
            pred.setContextDocSet(getContextDocSet());
            result = pred.evalPredicate(outerSequence, result, axis);
            //subsequent predicates operate on the result of the previous one
            outerSequence = null;
        }
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.exist.xquery.Step#analyze(org.exist.xquery.Expression)
     */
    public void analyze(AnalyzeContextInfo contextInfo) throws XPathException {
        this.parent = contextInfo.getParent();

        parentDeps = parent.getDependencies();
        if ((contextInfo.getFlags() & IN_UPDATE) > 0)
            inUpdate = true;
        if ((contextInfo.getFlags() & SINGLE_STEP_EXECUTION) > 0) {
            preload = true;
        }
        if ((contextInfo.getFlags() & NEED_INDEX_INFO) > 0) {
            useDirectAttrSelect = false;
        }
        if ((contextInfo.getFlags() & USE_TREE_TRAVERSAL) > 0) {
            useDirectChildSelect = true;
        }
        // Mark ".", which is expanded as self::node() by the parser
        //even though it may *also* be relevant with atomic sequences
        if (this.axis == Constants.SELF_AXIS && this.test.getType()== Type.NODE)
            contextInfo.addFlag(DOT_TEST);
        
        // TODO : log somewhere ?
        super.analyze(contextInfo);
    }

    /**
     * The method <code>eval</code>
     *
     * @param contextSequence a <code>Sequence</code> value
     * @param contextItem an <code>Item</code> value
     * @return a <code>Sequence</code> value
     * @exception XPathException if an error occurs
     */
    public Sequence eval(Sequence contextSequence, Item contextItem)
        throws XPathException {
        if (context.getProfiler().isEnabled()) {
            context.getProfiler().start(this);
            context.getProfiler().message(this, Profiler.DEPENDENCIES,
                                          "DEPENDENCIES",
                                          Dependency.getDependenciesName(this.getDependencies()));
            if (contextSequence != null)
                context.getProfiler().message(this, Profiler.START_SEQUENCES,
                                              "CONTEXT SEQUENCE", contextSequence);
            if (contextItem != null)
                context.getProfiler().message(this, Profiler.START_SEQUENCES,
                                              "CONTEXT ITEM", contextItem.toSequence());
        }

        Sequence result;
        if (contextItem != null) {
            contextSequence = contextItem.toSequence();
        }
        /*
         * if(contextSequence == null) //Commented because this the high level
         * result nodeset is *really* null result = NodeSet.EMPTY_SET; //Try to
         * return cached results else
         */
        if (cached != null && cached.isValid(contextSequence, contextItem)) {
	    
            // WARNING : commented since predicates are *also* applied below !
            // -pb
            /*
             * if (predicates.size() > 0) { applyPredicate(contextSequence,
             * cached.getResult()); } else {
             */
            result = cached.getResult();
            if (context.getProfiler().isEnabled()) {
            	LOG.debug("Using cached results");
            }
            context.getProfiler().message(this, Profiler.OPTIMIZATIONS,
                                          "Using cached results", result);

            // }
        } else if (needsComputation()) {
            if (contextSequence == null)
                throw new XPathException(getASTNode(), "XPDY0002 : undefined context sequence for '" + this.toString() + "'");
            switch (axis) {
                case Constants.DESCENDANT_AXIS:
                case Constants.DESCENDANT_SELF_AXIS:
                    result = getDescendants(context, contextSequence
                                            .toNodeSet());
                    break;
                case Constants.CHILD_AXIS:
                    //VirtualNodeSets may have modified the axis ; checking the type
                    //TODO : futher checks ?
                    if (this.test.getType() == Type.ATTRIBUTE) {
                        this.axis = Constants.ATTRIBUTE_AXIS;
                        result = getAttributes(context, contextSequence.toNodeSet());
                    } else {
                        result = getChildren(context, contextSequence.toNodeSet());
                    }
                    break;
                case Constants.ANCESTOR_SELF_AXIS:
                case Constants.ANCESTOR_AXIS:
                    result = getAncestors(context, contextSequence.toNodeSet());
                    break;
                case Constants.PARENT_AXIS:
                    result = getParents(context, contextSequence.toNodeSet());
                    break;
                case Constants.SELF_AXIS:
                    if (!(contextSequence instanceof VirtualNodeSet)
                        && Type.subTypeOf(contextSequence.getItemType(),
                                          Type.ATOMIC)) {
                        result = getSelfAtomic(contextSequence);
                    } else {
                        result = getSelf(context, contextSequence.toNodeSet());
                    }
                    break;
                case Constants.ATTRIBUTE_AXIS:
                case Constants.DESCENDANT_ATTRIBUTE_AXIS:
                    result = getAttributes(context, contextSequence.toNodeSet());
                    break;
                case Constants.PRECEDING_AXIS:
                    result = getPreceding(context, contextSequence.toNodeSet());
                    break;
                case Constants.FOLLOWING_AXIS:
                    result = getFollowing(context, contextSequence.toNodeSet());
                    break;
                case Constants.PRECEDING_SIBLING_AXIS:
                case Constants.FOLLOWING_SIBLING_AXIS:
                    result = getSiblings(context, contextSequence.toNodeSet());
                    break;
                default:
                    throw new IllegalArgumentException(
                                                       "Unsupported axis specified");
            }
        } else {
            result = NodeSet.EMPTY_SET;
        }
        // Caches the result
        if (axis != Constants.SELF_AXIS && contextSequence != null && contextSequence.isCacheable()) {
            // TODO : cache *after* removing duplicates ? -pb
            cached = new CachedResult(contextSequence, contextItem, result);
            registerUpdateListener();
        }
        // Remove duplicate nodes
        result.removeDuplicates();
        // Apply the predicate
        result = applyPredicate(contextSequence, result);

        if (context.getProfiler().isEnabled())
            context.getProfiler().end(this, "", result);
        //actualReturnType = result.getItemType();
        
        return result;
    }

    // Avoid unnecessary tests (these should be detected by the parser)
    private boolean needsComputation() {
        // TODO : log this ?
        switch (axis) {
            // Certainly not exhaustive
            case Constants.ANCESTOR_SELF_AXIS:
            case Constants.PARENT_AXIS:
            case Constants.SELF_AXIS:
                if (nodeTestType == null)
                    nodeTestType = new Integer(test.getType());
                if (nodeTestType.intValue() != Type.NODE
                    && nodeTestType.intValue() != Type.ELEMENT
                    && nodeTestType.intValue() != Type.PROCESSING_INSTRUCTION) {
                    if (context.getProfiler().isEnabled())
                        context.getProfiler().message(this,
                                                      Profiler.OPTIMIZATIONS, "OPTIMIZATION",
                                                      "avoid useless computations");
                    return false;
                }

        }
        return true;
    }

    /**
     * The method <code>getSelf</code>
     *
     * @param context a <code>XQueryContext</code> value
     * @param contextSet a <code>NodeSet</code> value
     * @return a <code>Sequence</code> value
     */
    protected Sequence getSelf(XQueryContext context, NodeSet contextSet) {
        if (test.getType() == Type.PROCESSING_INSTRUCTION) {
            VirtualNodeSet vset = new VirtualNodeSet(axis, test, contextId,
                                                     contextSet);
            vset.setInPredicate(Expression.NO_CONTEXT_ID != contextId);
            return vset;
        }

        if (test.isWildcardTest()) {
            if (nodeTestType == null) {
                nodeTestType = new Integer(test.getType());
            }
            if (Type.subTypeOf(nodeTestType.intValue(), Type.NODE)) {
                NodeSet result = null;
                if (Expression.NO_CONTEXT_ID != contextId && contextSet instanceof VirtualNodeSet) {
                    ((VirtualNodeSet) contextSet).setInPredicate(true);
                    ((VirtualNodeSet) contextSet).setSelfIsContext();
                    ((VirtualNodeSet) contextSet).setContextId(contextId);
                } else if (Type.subTypeOf(contextSet.getItemType(), Type.NODE)) {
                    NodeProxy p;
                    if (test.getType() != Type.NODE)
                        result = new ExtArrayNodeSet();
                    for (Iterator i = contextSet.iterator(); i.hasNext();) {
                        p = (NodeProxy) i.next();
                        if (test.matches(p)) {
                            p.addContextNode(contextId, p);
                            if (result != null)
                                result.add(p);
                        }
                    }
                }
                return result == null ? contextSet : result;
            } else {
                VirtualNodeSet vset = new VirtualNodeSet(axis, test, contextId,
                                                         contextSet);
                vset.setInPredicate(Expression.NO_CONTEXT_ID != contextId);
                return vset;
            }
        } else {
            DocumentSet docs = getDocumentSet(contextSet);
            NodeSelector selector = new SelfSelector(contextSet, contextId);
            ElementIndex index = context.getBroker().getElementIndex();
            if (context.getProfiler().isEnabled())
                context.getProfiler().message(this, Profiler.OPTIMIZATIONS,
                                              "OPTIMIZATION",
                                              "Using structural index '" + index.toString() + "'");
            return index.findElementsByTagName(ElementValue.ELEMENT, docs, test
                                               .getName(), selector);
        }
    }

    /**
     * The method <code>getSelfAtomic</code>
     *
     * @param contextSequence a <code>Sequence</code> value
     * @return a <code>Sequence</code> value
     * @exception XPathException if an error occurs
     */
    protected Sequence getSelfAtomic(Sequence contextSequence)
        throws XPathException {
        if (!test.isWildcardTest())
            throw new XPathException(getASTNode(), test.toString()
                                     + " cannot be applied to an atomic value.");
        return contextSequence;
    }

    /**
     * The method <code>getAttributes</code>
     *
     * @param context a <code>XQueryContext</code> value
     * @param contextSet a <code>NodeSet</code> value
     * @return a <code>NodeSet</code> value
     */
    protected NodeSet getAttributes(XQueryContext context, NodeSet contextSet) {
        boolean selectDirect = false;
        if (useDirectAttrSelect && axis == Constants.ATTRIBUTE_AXIS) {
            if (contextSet instanceof VirtualNodeSet)
                selectDirect = ((VirtualNodeSet) contextSet).preferTreeTraversal()
                    && contextSet.getLength() < ATTR_DIRECT_SELECT_THRESHOLD;
            else
                selectDirect = contextSet.getLength() < ATTR_DIRECT_SELECT_THRESHOLD;
        }
        if (selectDirect) {
            if (context.getProfiler().isEnabled())
                context.getProfiler().message(this, Profiler.OPTIMIZATIONS,
                                              "OPTIMIZATION", "direct attribute selection");
            if (contextSet.isEmpty())
            	return NodeSet.EMPTY_SET;
            NodeProxy proxy = contextSet.get(0);
            if (proxy != null)
                return contextSet.directSelectAttribute(test, contextId);
        }
        if (test.isWildcardTest()) {
            NodeSet result = new VirtualNodeSet(axis, test, contextId, contextSet);
            ((VirtualNodeSet) result).setInPredicate(Expression.NO_CONTEXT_ID != contextId);
            return result;
            // if there's just a single known node in the context, it is faster
            // do directly search for the attribute in the parent node.
        }
        if (preloadNodeSets()) {
            DocumentSet docs = getDocumentSet(contextSet);
            if (!optimized && (currentSet == null || currentDocs == null
                || !(docs.equals(currentDocs)))) {
                ElementIndex index = context.getBroker().getElementIndex();
                if (context.getProfiler().isEnabled())
                    context.getProfiler().message(this, Profiler.OPTIMIZATIONS,
                                                  "OPTIMIZATION",
                                                  "Using structural index '" + index.toString() + "'");
                // TODO : why a null selector here ? We have one below !
                currentSet = index.findElementsByTagName(
                                                         ElementValue.ATTRIBUTE, docs, test.getName(), null);
                currentDocs = docs;
                registerUpdateListener();
            }
            switch (axis) {
                case Constants.ATTRIBUTE_AXIS:
                    return currentSet.selectParentChild(contextSet,
                                                        NodeSet.DESCENDANT, contextId);
                case Constants.DESCENDANT_ATTRIBUTE_AXIS:
                    return currentSet.selectAncestorDescendant(contextSet,
                                                               NodeSet.DESCENDANT, false, contextId);
                default:
                    throw new IllegalArgumentException(
                                                       "Unsupported axis specified");
            }
        } else {
            NodeSelector selector;
            DocumentSet docs = getDocumentSet(contextSet);
            // TODO : why a selector here ? We havn't one above !
            switch (axis) {
                case Constants.ATTRIBUTE_AXIS:
                    selector = new ChildSelector(contextSet, contextId);
                    break;
                case Constants.DESCENDANT_ATTRIBUTE_AXIS:
                    selector = new DescendantSelector(contextSet, contextId);
                    break;
                default:
                    throw new IllegalArgumentException(
                                                       "Unsupported axis specified");
            }
            ElementIndex index = context.getBroker().getElementIndex();
            if (context.getProfiler().isEnabled())
                context.getProfiler().message(this, Profiler.OPTIMIZATIONS,
                                              "OPTIMIZATION",
                                              "Using structural index '" + index.toString() + "'");
            if (contextSet instanceof ExtArrayNodeSet && !contextSet.getProcessInReverseOrder()) {
                return index.findDescendantsByTagName(ElementValue.ATTRIBUTE, test.getName(), axis,
                                                      docs, (ExtArrayNodeSet) contextSet, contextId);
            } else {
                return index.findElementsByTagName(ElementValue.ATTRIBUTE, docs, test.getName(), selector);
            }
        }
    }

    /**
     * The method <code>getChildren</code>
     *
     * @param context a <code>XQueryContext</code> value
     * @param contextSet a <code>NodeSet</code> value
     * @return a <code>NodeSet</code> value
     */
    protected NodeSet getChildren(XQueryContext context, NodeSet contextSet) {
        if (test.isWildcardTest() || test.getType() == Type.PROCESSING_INSTRUCTION) {
            // test is one out of *, text(), node() including processing-instruction(targetname)
            VirtualNodeSet vset = new VirtualNodeSet(axis, test, contextId,
                                                     contextSet);
            vset.setInPredicate(Expression.NO_CONTEXT_ID != contextId);
            return vset;
        }

//        IndexStatistics stats = (IndexStatistics) context.getBroker().getBrokerPool().
//            getIndexManager().getIndexById(IndexStatistics.ID);
//        int parentDepth = stats.getMaxParentDepth(test.getName());
//        LOG.debug("parentDepth for " + test.getName() + ": " + parentDepth);

        if (useDirectChildSelect) {
            ExtArrayNodeSet result = new ExtArrayNodeSet();
            for (Iterator i = contextSet.iterator(); i.hasNext(); ) {
                NodeProxy p = (NodeProxy) i.next();
                result.addAll(p.directSelectChild(test.getName(), contextId));
            }
            return result;
        } else if (preloadNodeSets()) {
            DocumentSet docs = getDocumentSet(contextSet);
            // TODO : understand why this one is different from the other ones
            if (!optimized && (currentSet == null || currentDocs == null
                || !(docs == currentDocs || docs.equals(currentDocs)))) {
                ElementIndex index = context.getBroker().getElementIndex();
                if (context.getProfiler().isEnabled())
                    context.getProfiler().message(this, Profiler.OPTIMIZATIONS,
                                                  "OPTIMIZATION",
                                                  "Using structural index '" + index.toString() + "'");
                currentSet = index.findElementsByTagName(ElementValue.ELEMENT,
                                                         docs, test.getName(), null);
                currentDocs = docs;
                registerUpdateListener();
            }
            return currentSet.selectParentChild(contextSet, NodeSet.DESCENDANT,
                                                contextId);
        } else {
            ElementIndex index = context.getBroker().getElementIndex();
            if (context.getProfiler().isEnabled())
                context.getProfiler().message(this, Profiler.OPTIMIZATIONS,
                                              "OPTIMIZATION",
                                              "Using structural index '" + index.toString() + "'");
            DocumentSet docs = getDocumentSet(contextSet);
            if (contextSet instanceof ExtArrayNodeSet && !contextSet.getProcessInReverseOrder()) {
            	return index.findDescendantsByTagName(ElementValue.ELEMENT, test.getName(), axis,
                                                      docs, (ExtArrayNodeSet) contextSet, contextId);
            } else {
                //            	if (contextSet instanceof VirtualNodeSet)
                //            		((VirtualNodeSet)contextSet).realize();
            	NodeSelector selector = new ChildSelector(contextSet, contextId);
            	return index.findElementsByTagName(ElementValue.ELEMENT, docs, test
                                                   .getName(), selector);
            }
        }
    }

    /**
     * The method <code>getDescendants</code>
     *
     * @param context a <code>XQueryContext</code> value
     * @param contextSet a <code>NodeSet</code> value
     * @return a <code>NodeSet</code> value
     */
    protected NodeSet getDescendants(XQueryContext context, NodeSet contextSet) {
        if (test.isWildcardTest() || test.getType() == Type.PROCESSING_INSTRUCTION) {
            // test is one out of *, text(), node() including processing-instruction(targetname)
            VirtualNodeSet vset = new VirtualNodeSet(axis, test, contextId,
                                                     contextSet);
            vset.setInPredicate(Expression.NO_CONTEXT_ID != contextId);
            return vset;
        } else if (preloadNodeSets()) {
            DocumentSet docs = getDocumentSet(contextSet);
            // TODO : understand why this one is different from the other ones
            if (!optimized && (currentSet == null || currentDocs == null
                || !(docs == currentDocs || docs.equals(currentDocs)))) {
                ElementIndex index = context.getBroker().getElementIndex();
                if (context.getProfiler().isEnabled())
                    context.getProfiler().message(this, Profiler.OPTIMIZATIONS,
                                                  "OPTIMIZATION",
                                                  "Using structural index '" + index.toString() + "'");
                currentSet = index.findElementsByTagName(ElementValue.ELEMENT,
                                                         docs, test.getName(), null);
                currentDocs = docs;
                registerUpdateListener();
            }
            switch (axis) {
                case Constants.DESCENDANT_SELF_AXIS:
                    NodeSet tempSet = currentSet.selectAncestorDescendant(contextSet,
                                                                          NodeSet.DESCENDANT, true, contextId);
                    return tempSet;
                case Constants.DESCENDANT_AXIS:
                    return currentSet.selectAncestorDescendant(contextSet,
                                                               NodeSet.DESCENDANT, false, contextId);
                default:
                    throw new IllegalArgumentException(
                                                       "Unsupported axis specified");
            }
        } else {
            NodeSelector selector;
            DocumentSet docs = contextSet.getDocumentSet();
            switch (axis) {
                case Constants.DESCENDANT_SELF_AXIS:
                    selector = new DescendantOrSelfSelector(contextSet,
                                                            contextId);
                    break;
                case Constants.DESCENDANT_AXIS:
                    selector = new DescendantSelector(contextSet, contextId);
                    break;
                default:
                    throw new IllegalArgumentException(
                                                       "Unsupported axis specified");
            }

            ElementIndex index = context.getBroker().getElementIndex();
            if (context.getProfiler().isEnabled()) {
                context.getProfiler().message(this, Profiler.OPTIMIZATIONS,
                                              "OPTIMIZATION",
                                              "Using structural index '" + index.toString() + "'");
            }
            if (contextSet instanceof ExtArrayNodeSet) {
            	return index.findDescendantsByTagName(ElementValue.ELEMENT, test.getName(), axis,
                                                      docs, (ExtArrayNodeSet) contextSet, contextId);
            } else {
                return index.findElementsByTagName(ElementValue.ELEMENT, docs, test.getName(), selector);
            }

        }
    }

    /**
     * The method <code>getSiblings</code>
     *
     * @param context a <code>XQueryContext</code> value
     * @param contextSet a <code>NodeSet</code> value
     * @return a <code>NodeSet</code> value
     */
    protected NodeSet getSiblings(XQueryContext context, NodeSet contextSet) {
        if (test.getType() == Type.PROCESSING_INSTRUCTION) {
            VirtualNodeSet vset = new VirtualNodeSet(axis, test, contextId,
                                                     contextSet);
            
            vset.setInPredicate(Expression.NO_CONTEXT_ID != contextId);
            return vset;
        }
        if (test.isWildcardTest()) {
            ExtArrayNodeSet result = new ExtArrayNodeSet(contextSet.getLength());
            SiblingVisitor visitor = new SiblingVisitor(result);
            for (Iterator i = contextSet.iterator(); i.hasNext();) {
                NodeProxy current = (NodeProxy) i.next();
                NodeId parentId = current.getNodeId().getParentId();
                if(parentId.getTreeLevel() == 1 && current.getDocument().getCollection().isTempCollection())
                    continue;
                StoredNode parentNode = context.getBroker().objectWith(current.getOwnerDocument(), parentId);
            	visitor.setContext(current);
            	parentNode.accept(visitor);
            }
            return result;
        } else {
            DocumentSet docs = getDocumentSet(contextSet);
            if (currentSet == null || currentDocs == null
                || !(docs.equals(currentDocs))) {
                ElementIndex index = context.getBroker().getElementIndex();
                if (context.getProfiler().isEnabled())
                    context.getProfiler().message(this, Profiler.OPTIMIZATIONS,
                                                  "OPTIMIZATION",
                                                  "Using structural index '" + index.toString() + "'");
                currentSet = index.findElementsByTagName(ElementValue.ELEMENT,
                                                         docs, test.getName(), null);
                currentDocs = docs;
                registerUpdateListener();
            }
            switch (axis) {
                case Constants.PRECEDING_SIBLING_AXIS:
                    return currentSet.selectPrecedingSiblings(contextSet, contextId);
                case Constants.FOLLOWING_SIBLING_AXIS:
                    return currentSet.selectFollowingSiblings(contextSet, contextId);
                default:
                    throw new IllegalArgumentException(
                                                       "Unsupported axis specified");
            }
        }
    }
    
    private class SiblingVisitor implements NodeVisitor {
    	
    	private ExtArrayNodeSet resultSet;
    	private NodeProxy contextNode;
    	
    	public SiblingVisitor(ExtArrayNodeSet resultSet) {
            this.resultSet = resultSet;
    	}
    	
    	public void setContext(NodeProxy contextNode) {
            this.contextNode = contextNode;
    	}
    	
    	public boolean visit(StoredNode current) {
            if (contextNode.getNodeId().getTreeLevel() == current.getNodeId().getTreeLevel()) {
                int cmp = current.getNodeId().compareTo(contextNode.getNodeId());
                if (((axis == Constants.FOLLOWING_SIBLING_AXIS && cmp > 0) || 
                     (axis == Constants.PRECEDING_SIBLING_AXIS && cmp < 0)) &&
                    test.matches(current)) {
                    NodeProxy sibling = resultSet.get((DocumentImpl) current.getOwnerDocument(), current.getNodeId());
                    if (sibling == null) {
                        sibling = new NodeProxy((DocumentImpl) current.getOwnerDocument(), current.getNodeId(),
                                                current.getInternalAddress());
                        if (Expression.NO_CONTEXT_ID != contextId) {
                            sibling.addContextNode(contextId, contextNode);
                        } else
                            sibling.copyContext(contextNode);
                        resultSet.add(sibling);
                        resultSet.setSorted(sibling.getDocument(), true);
                    } else if (Expression.NO_CONTEXT_ID != contextId)
                        sibling.addContextNode(contextId, contextNode);
                }
            }
            return true;
    	}
    }

    /**
     * The method <code>getPreceding</code>
     *
     * @param context a <code>XQueryContext</code> value
     * @param contextSet a <code>NodeSet</code> value
     * @return a <code>NodeSet</code> value
     * @exception XPathException if an error occurs
     */
    protected NodeSet getPreceding(XQueryContext context, NodeSet contextSet)
        throws XPathException {
        if (test.getType() == Type.PROCESSING_INSTRUCTION) {
            VirtualNodeSet vset = new VirtualNodeSet(axis, test, contextId,
                                                     contextSet);
            
            vset.setInPredicate(Expression.NO_CONTEXT_ID != contextId);
            return vset;
        }
        if (test.isWildcardTest()) {
            // TODO : throw an exception here ! Don't let this pass through
            return NodeSet.EMPTY_SET;
        } else {
            DocumentSet docs = getDocumentSet(contextSet);
            if (currentSet == null || currentDocs == null
                || !(docs.equals(currentDocs))) {
                ElementIndex index = context.getBroker().getElementIndex();
                if (context.getProfiler().isEnabled())
                    context.getProfiler().message(this, Profiler.OPTIMIZATIONS,
                                                  "OPTIMIZATION",
                                                  "Using structural index '" + index.toString() + "'");
                currentSet = index.findElementsByTagName(ElementValue.ELEMENT,
                                                         docs, test.getName(), null);
                currentDocs = docs;
                registerUpdateListener();
            }
            return currentSet.selectPreceding(contextSet);
        }
    }

    /**
     * The method <code>getFollowing</code>
     *
     * @param context a <code>XQueryContext</code> value
     * @param contextSet a <code>NodeSet</code> value
     * @return a <code>NodeSet</code> value
     * @exception XPathException if an error occurs
     */
    protected NodeSet getFollowing(XQueryContext context, NodeSet contextSet)
        throws XPathException {
        if (test.getType() == Type.PROCESSING_INSTRUCTION) {
            VirtualNodeSet vset = new VirtualNodeSet(axis, test, contextId,
                                                     contextSet);
            
            vset.setInPredicate(Expression.NO_CONTEXT_ID != contextId);
            return vset;
        }
        if (test.isWildcardTest() && test.getType() != Type.PROCESSING_INSTRUCTION) {
            // TODO : throw an exception here ! Don't let this pass through
            return NodeSet.EMPTY_SET;
        } else {
            DocumentSet docs = getDocumentSet(contextSet);
            if (currentSet == null || currentDocs == null
                || !(docs.equals(currentDocs))) {
                ElementIndex index = context.getBroker().getElementIndex();
                if (context.getProfiler().isEnabled())
                    context.getProfiler().message(this, Profiler.OPTIMIZATIONS,
                                                  "OPTIMIZATION",
                                                  "Using structural index '" + index.toString() + "'");
                currentSet = index.findElementsByTagName(ElementValue.ELEMENT,
                                                         docs, test.getName(), null);
                currentDocs = docs;
                registerUpdateListener();
            }
            return currentSet.selectFollowing(contextSet);
        }
    }

    /**
     * The method <code>getAncestors</code>
     *
     * @param context a <code>XQueryContext</code> value
     * @param contextSet a <code>NodeSet</code> value
     * @return a <code>NodeSet</code> value
     */
    protected NodeSet getAncestors(XQueryContext context, NodeSet contextSet) {
        if (test.isWildcardTest()) {
            NodeSet result = new ExtArrayNodeSet();
            result.setProcessInReverseOrder(true);
            for (Iterator i = contextSet.iterator(); i.hasNext();) {
                NodeProxy current = (NodeProxy) i.next();
                NodeProxy ancestor;
                if (axis == Constants.ANCESTOR_SELF_AXIS
                    && test.matches(current)) {
                    ancestor = new NodeProxy(current.getDocument(), current.getNodeId(), 
                                             Node.ELEMENT_NODE, current.getInternalAddress());
                    NodeProxy t = result.get(ancestor);
                    if (t == null) {
                        if (Expression.NO_CONTEXT_ID != contextId)
                            ancestor.addContextNode(contextId, current);
                        else
                            ancestor.copyContext(current);
                        ancestor.addMatches(current);
                        result.add(ancestor);
                    } else {
                        t.addContextNode(contextId, current);
                        t.addMatches(current);
                    }
                }
                NodeId parentID = current.getNodeId().getParentId();
                while (parentID != null) {
                    ancestor = new NodeProxy(current.getDocument(), parentID, Node.ELEMENT_NODE);
                    // Filter out the temporary nodes wrapper element
                    if (parentID != NodeId.DOCUMENT_NODE && 
                        !(parentID.getTreeLevel() == 1  && current.getDocument().getCollection().isTempCollection())) {
                        if (test.matches(ancestor)) {
                            NodeProxy t = result.get(ancestor);
                            if (t == null) {
                                if (Expression.NO_CONTEXT_ID != contextId)
                                    ancestor.addContextNode(contextId, current);
                                else
                                    ancestor.copyContext(current);
                                ancestor.addMatches(current);
                                result.add(ancestor);
                            } else {
                                t.addContextNode(contextId, current);
                                t.addMatches(current);
                            }
                        }
                    }
                    parentID = parentID.getParentId();
                }
            }
            return result;
        } else if (preloadNodeSets()) {
            DocumentSet docs = getDocumentSet(contextSet);
            if (!optimized && (currentSet == null || currentDocs == null
                || !(docs.equals(currentDocs)))) {
                ElementIndex index = context.getBroker().getElementIndex();
                if (context.getProfiler().isEnabled())
                    context.getProfiler().message(this, Profiler.OPTIMIZATIONS,
                                                  "OPTIMIZATION",
                                                  "Using structural index '" + index.toString() + "'");
                currentSet = index.findElementsByTagName(ElementValue.ELEMENT,
                                                         docs, test.getName(), null);
                currentDocs = docs;
                registerUpdateListener();
            }
            switch (axis) {
                case Constants.ANCESTOR_SELF_AXIS:
                    return currentSet.selectAncestors(contextSet, true,
                                                      contextId);
                case Constants.ANCESTOR_AXIS:
                    return currentSet.selectAncestors(contextSet, false,
                                                      contextId);
                default:
                    throw new IllegalArgumentException(
                                                       "Unsupported axis specified");
            }
        } else {
            NodeSelector selector;
            DocumentSet docs = getDocumentSet(contextSet);
            switch (axis) {
                case Constants.ANCESTOR_SELF_AXIS:
                    selector = new AncestorSelector(contextSet, contextId, true);
                    break;
                case Constants.ANCESTOR_AXIS:
                    selector = new AncestorSelector(contextSet, contextId,
                                                    false);
                    break;
                default:
                    throw new IllegalArgumentException(
                                                       "Unsupported axis specified");
            }
            ElementIndex index = context.getBroker().getElementIndex();
            if (context.getProfiler().isEnabled())
                context.getProfiler().message(this, Profiler.OPTIMIZATIONS,
                                              "OPTIMIZATION",
                                              "Using structural index '" + index.toString() + "'");
            return index.findElementsByTagName(ElementValue.ELEMENT, docs, test
                                               .getName(), selector);
        }
    }

    /**
     * The method <code>getParents</code>
     *
     * @param context a <code>XQueryContext</code> value
     * @param contextSet a <code>NodeSet</code> value
     * @return a <code>NodeSet</code> value
     */
    protected NodeSet getParents(XQueryContext context, NodeSet contextSet) {
        if (test.isWildcardTest()) {
            NodeSet temp = contextSet.getParents(contextId);
            NodeSet result = new ExtArrayNodeSet();
            NodeProxy p;
            for (Iterator i = temp.iterator(); i.hasNext(); ) {
                p = (NodeProxy) i.next();

                if (test.matches(p)) {
                    result.add(p);
                }
            }
            return result;
        } else if (preloadNodeSets()) {
            DocumentSet docs = getDocumentSet(contextSet);
            if (!optimized && (currentSet == null || currentDocs == null
                || !(docs.equals(currentDocs)))) {
                ElementIndex index = context.getBroker().getElementIndex();
                if (context.getProfiler().isEnabled())
                    context.getProfiler().message(this, Profiler.OPTIMIZATIONS,
                                                  "OPTIMIZATION",
                                                  "Using structural index '" + index.toString() + "'");
                currentSet = index.findElementsByTagName(ElementValue.ELEMENT,
                                                         docs, test.getName(), null);
                currentDocs = docs;
                registerUpdateListener();
            }
            return contextSet.selectParentChild(currentSet, NodeSet.ANCESTOR); 
        } else {
            DocumentSet docs = getDocumentSet(contextSet);
            NodeSelector selector = new ParentSelector(contextSet, contextId);
            ElementIndex index = context.getBroker().getElementIndex();
            if (context.getProfiler().isEnabled())
                context.getProfiler().message(this, Profiler.OPTIMIZATIONS,
                                              "OPTIMIZATION",
                                              "Using structural index '" + index.toString() + "'");
            return index.findElementsByTagName(ElementValue.ELEMENT, docs, test
                                               .getName(), selector);
        }
    }

    /**
     * The method <code>getDocumentSet</code>
     *
     * @param contextSet a <code>NodeSet</code> value
     * @return a <code>DocumentSet</code> value
     */
    protected DocumentSet getDocumentSet(NodeSet contextSet) {
        DocumentSet ds = getContextDocSet();
        if (ds == null)
            ds = contextSet.getDocumentSet();
        return ds;
    }

    /**
     * The method <code>getParent</code>
     *
     * @return an <code>Expression</code> value
     */
    public Expression getParent() {
        return this.parent;
    }
    
    /**
     * The method <code>setUseDirectAttrSelect</code>
     *
     * @param useDirectAttrSelect a <code>boolean</code> value
     */
    public void setUseDirectAttrSelect(boolean useDirectAttrSelect) {
        this.useDirectAttrSelect = useDirectAttrSelect;
    }

    /**
     * The method <code>registerUpdateListener</code>
     *
     */
    protected void registerUpdateListener() {
        if (listener == null) {
            listener = new UpdateListener() {
                    public void documentUpdated(DocumentImpl document, int event) {
                        if (document == null || event == UpdateListener.ADD || event == UpdateListener.REMOVE) {
                            // clear all
                            currentDocs = null;
                            currentSet = null;
                            cached = null;
                        } else {
                            if (currentDocs != null
                                && currentDocs.contains(document.getDocId())) {
                                currentDocs = null;
                                currentSet = null;
                            }
                            if (cached != null
                                && cached.getResult().getDocumentSet()
                                .contains(document.getDocId()))
                                cached = null;
                        }
                    }

                    public void nodeMoved(NodeId oldNodeId, StoredNode newNode) {
                    }


                    public void unsubscribe() {
                        LocationStep.this.listener = null;
                    }

                    public void debug() {
                        LOG.debug("UpdateListener: Line: " + LocationStep.this.toString() +
                                  "; id: " + LocationStep.this.getExpressionId());
                    }
                };
            context.registerUpdateListener(listener);
        }
    }

    /**
     * The method <code>accept</code>
     *
     * @param visitor an <code>ExpressionVisitor</code> value
     */
    public void accept(ExpressionVisitor visitor) {
        visitor.visitLocationStep(this);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.exist.xquery.Step#resetState()
     */
    public void resetState(boolean postOptimization) {
        super.resetState(postOptimization);
        if (!postOptimization) {
            currentSet = null;
            currentDocs = null;
            optimized = false;
            cached = null;
            listener = null;
        }
    }

}
