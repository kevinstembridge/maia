package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.DataClassDef

class DataClassTypescriptRenderer(dataClassDef: DataClassDef) : TypescriptInterfaceDtoRenderer(
    dataClassDef.renderedTypescriptFilePath,
    dataClassDef.uqcn,
    dataClassDef.classFieldDefs,
    emptySet()
)
