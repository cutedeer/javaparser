/*
 * Copyright (C) 2015-2016 Federico Tomassetti
 * Copyright (C) 2017-2020 The JavaParser Team.
 *
 * This file is part of JavaParser.
 *
 * JavaParser can be used either under the terms of
 * a) the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * b) the terms of the Apache License
 *
 * You should have received a copy of both licenses in LICENCE.LGPL and
 * LICENCE.APACHE. Please refer to those files for details.
 *
 * JavaParser is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 */

package com.github.javaparser.symbolsolver.logic;

import com.github.javaparser.resolution.MethodUsage;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedTypeParameterDeclaration;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.utils.Pair;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Common ancestor for most types.
 *
 * @author Federico Tomassetti
 */
public abstract class AbstractTypeDeclaration implements ResolvedReferenceTypeDeclaration {

    @Override
    public final Set<MethodUsage> getAllMethods() {
        Set<MethodUsage> methods = new HashSet<>();

        Set<String> methodsSignatures = new HashSet<>();

        for (ResolvedMethodDeclaration methodDeclaration : getDeclaredMethods()) {
            MethodUsage methodUsage = new MethodUsage(methodDeclaration);
            methods.add(methodUsage);
            methodsSignatures.add(getSignature(methodUsage));
        }

        for (ResolvedReferenceType ancestor : getAllAncestors()) {
            List<Pair<ResolvedTypeParameterDeclaration, ResolvedType>> typeParametersMap = ancestor.getTypeParametersMap();
            for (MethodUsage ancestorMethodUsage : ancestor.getDeclaredMethods()) {
                MethodUsage methodUsage = ancestorMethodUsage;
                for (Pair<ResolvedTypeParameterDeclaration, ResolvedType> p : typeParametersMap) {
                    methodUsage = methodUsage.replaceTypeParameter(p.a, p.b);
                }
                String signature = getSignature(methodUsage);
                if (!methodsSignatures.contains(signature)) {
                    methodsSignatures.add(signature);
                    methods.add(ancestorMethodUsage);
                }
            }
        }

        return methods;
    }

    private String getSignature(MethodUsage mu) {
        StringBuilder sb = new StringBuilder();
        sb.append(mu.getName());
        sb.append("(");
        for (int i = 0; i < mu.getNoParams(); i++) {
            if (i != 0) {
                sb.append(", ");
            }
            sb.append(mu.getParamType(i).describe());
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public final boolean isFunctionalInterface() {
        return FunctionalInterfaceLogic.getFunctionalMethod(this).isPresent();
    }

}
