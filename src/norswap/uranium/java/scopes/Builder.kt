package norswap.uranium.java.scopes
import norswap.lang.java8.ast.AnnotationElemDecl
import norswap.lang.java8.ast.Block
import norswap.lang.java8.ast.ConstructorDecl
import norswap.lang.java8.ast.CtorCall
import norswap.lang.java8.ast.EnumConstant
import norswap.lang.java8.ast.File
import norswap.lang.java8.ast.FormalParameter
import norswap.lang.java8.ast.FormalParameters
import norswap.lang.java8.ast.Import
import norswap.lang.java8.ast.Lambda
import norswap.lang.java8.ast.MethodDecl
import norswap.lang.java8.ast.Package
import norswap.lang.java8.ast.TypeDecl
import norswap.lang.java8.ast.TypeDeclKind
import norswap.lang.java8.ast.TypeParam
import norswap.lang.java8.ast.UntypedParameters
import norswap.lang.java8.ast.VarDecl
import norswap.uranium.Propagator
import norswap.uranium.java.model.AnonymousSourceClass
import norswap.uranium.java.model.Klass
import norswap.uranium.java.model.SourceAnnotation
import norswap.uranium.java.model.SourceClass
import norswap.uranium.java.model.SourceConstructor
import norswap.uranium.java.model.SourceEnumConstant
import norswap.uranium.java.model.SourceField
import norswap.uranium.java.model.SourceMethod
import norswap.uranium.java.model.SourceParameter
import norswap.uranium.java.model.UntypedSourceParameter
import norswap.uranium.java.model.scopes.BlockScope
import norswap.uranium.java.model.scopes.FileScope
import norswap.uranium.java.model.scopes.MethodScope
import norswap.uranium.java.model.scopes.PackageScope
import norswap.uranium.java.model.scopes.SourceAnnotationScope
import norswap.uranium.java.model.scopes.SourceClassScope
import norswap.uranium.java.resolver
import norswap.utils.multimap.append

class ScopesBuilder
{
    // ---------------------------------------------------------------------------------------------

    internal lateinit var propagator: Propagator

    // ---------------------------------------------------------------------------------------------

    private val default_scope = PackageScope(null)

    // ---------------------------------------------------------------------------------------------

    private lateinit var file: FileScope

    private var type: Klass? = null

    private var type_scope: SourceClassScope? = null

    private var block: BlockScope? = null

    private var anonymous_klass_index = 0

    // ---------------------------------------------------------------------------------------------

    fun visit_file (node: File, start: Boolean)
    {
        this.file = FileScope(node, default_scope)
        this.type = null
    }

    // ---------------------------------------------------------------------------------------------

    fun visit_pkg (node: Package, start: Boolean)
    {
        file.pkg = PackageScope(node)
    }

    // ---------------------------------------------------------------------------------------------

    fun visit_import (node: Import, start: Boolean)
    {
        val cano_name = node.name.joinToString(".")

        if (node.wildcard) {
            if (node.static)
                file.wildcard_static_imports += cano_name
            else
                file.wildcard_imports += cano_name
        }
        else {
            val simple_name = node.name.last()

            if (node.static)
                file.single_static_imports[simple_name] = cano_name
            else
                file.single_imports[simple_name] = cano_name
        }
    }

    // ---------------------------------------------------------------------------------------------

    fun visit_class (node: TypeDecl, start: Boolean)
    {
        val type =
            if (node.kind == TypeDeclKind.ANNOTATION)
                SourceAnnotation(node, block, type, file)
            else
                SourceClass(node, block, type, file)

        val scope       = type.scope
        val outer_class = this.type_scope
        val outer_block = this.block
        this.type       = type
        this.type_scope = scope as SourceClassScope // TODO horribly wrong
        anonymous_klass_index = 0

        if (outer_block != null)
            outer_block.classes[type.name] = type
        else if (outer_class != null)
            outer_class.classes[type.name] = type.binary_name
        else
            this.file.pkg.classes[type.name] = type

        // propagator.resolver.add_source_class(type)
    }

    // ---------------------------------------------------------------------------------------------

    fun visit_ctor_call (node: CtorCall, start: Boolean)
    {
        if (node.body == null) return

        val type = AnonymousSourceClass(node, block, type!!, file, anonymous_klass_index++)
    }

    // ---------------------------------------------------------------------------------------------

    fun visit_ctor (node: ConstructorDecl)
    {
        val ctor = SourceConstructor(node, block, this.type!!)
        this.type_scope!!.constructors += ctor
        val scope = ctor.scope
        this.block = scope

        node.tparams.forEach {
            scope.type_params[it.name] = it
        }

        node.params.params.forEach {
            scope.variables[it.name] = SourceParameter(it)
        }
    }

    // ---------------------------------------------------------------------------------------------

    fun visit_method (node: MethodDecl, start: Boolean)
    {
        val method = SourceMethod(node, block, this.type!!)
        this.type_scope!!.methods.append(node.name, method)
        val scope = method.scope
        this.block = scope

        node.tparams.forEach {
            scope.type_params[it.name] = it
        }

        node.params.params.forEach {
            scope.variables[it.name] = SourceParameter(it)
        }
    }

    // ---------------------------------------------------------------------------------------------

    fun visit_var (node: VarDecl, start: Boolean)
    {
        val block = this.block
        if (block != null) {
            for (decl in node.declarators)
                block.variables[decl.id.iden] = SourceField(node, decl)
        }
        else {
            val scope = this.type_scope!!
            for (decl in node.declarators)
                scope.fields[decl.id.iden] = SourceField(node, decl)
        }
    }

    // ---------------------------------------------------------------------------------------------

    fun visit_type_param (node: TypeParam, start: Boolean)
    {
        this.type_scope!!.type_params[node.name] = node
    }

    // ---------------------------------------------------------------------------------------------

    fun visit_lambda (node: Lambda, start: Boolean)
    {
        val scope = MethodScope(node, this.block, this.type!!)
        this.block = scope

        when (node.params) {
            is FormalParameters ->
                node.params.params.forEach {
                    scope.variables[it.name] = SourceParameter(it)
                }
            is UntypedParameters ->
                node.params.params.forEach {
                    scope.variables[it] = UntypedSourceParameter(it)
                }
        }
    }

    // ---------------------------------------------------------------------------------------------

    fun visit_block (node: Block, start: Boolean)
    {
        val scope = BlockScope(node, this.block, this.type!!)
        this.block = scope

        node.stmts.filterIsInstance<VarDecl>().forEach {
            for (decl in it.declarators)
                scope.variables[decl.id.iden] = SourceField(it, decl)
        }

        // TODO declare before use restriction
        // TODO shadowing
    }

    // ---------------------------------------------------------------------------------------------

    fun visit_enum_constant (node: EnumConstant, start: Boolean)
    {
        // TODO it's static bitch
        type_scope!!.fields[node.name] = SourceEnumConstant(node)
    }

    // ---------------------------------------------------------------------------------------------

    fun visit_annotation_elem (node: AnnotationElemDecl, start: Boolean)
    {
        // TODO register in SourceAnnotationScope.elements
    }

    // ---------------------------------------------------------------------------------------------
}