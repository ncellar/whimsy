package norswap.uranium.java
import norswap.lang.java8.ast.AnnotationElemDecl
import norswap.lang.java8.ast.CatchClause
import norswap.lang.java8.ast.ConstructorDecl
import norswap.lang.java8.ast.CtorCall
import norswap.lang.java8.ast.EnhancedFor
import norswap.lang.java8.ast.EnumConstant
import norswap.lang.java8.ast.File as AFile
import norswap.lang.java8.ast.FormalParameters
import norswap.lang.java8.ast.Identifier
import norswap.lang.java8.ast.Import
import norswap.lang.java8.ast.LabelledStmt
import norswap.lang.java8.ast.MethodDecl
import norswap.lang.java8.ast.Package as APackage
import norswap.lang.java8.ast.TryStmt
import norswap.lang.java8.ast.TypeDecl
import norswap.lang.java8.ast.TypeParam
import norswap.lang.java8.ast.UntypedParameters
import norswap.lang.java8.ast.VarDecl
import norswap.lang.java8.ast.Block  as ASTBlock
import norswap.lang.java8.ast.Lambda as ASTLambda
import norswap.uranium.Propagator
import norswap.uranium.java.model.Klass
import norswap.uranium.java.model.Package
import norswap.uranium.java.model.source.Block
import norswap.uranium.java.model.source.CatchParameter
import norswap.uranium.java.model.source.ControlFlow
import norswap.uranium.java.model.source.File
import norswap.uranium.java.model.source.ForParameter
import norswap.uranium.java.model.source.Lambda
import norswap.uranium.java.model.source.Scope
import norswap.uranium.java.model.source.SourceAnnotationElement
import norswap.uranium.java.model.source.SourceClass
import norswap.uranium.java.model.source.SourceConstructor
import norswap.uranium.java.model.source.SourceEnumConstant
import norswap.uranium.java.model.source.SourceField
import norswap.uranium.java.model.source.SourceMethod
import norswap.uranium.java.model.source.SourceTypeParameter
import norswap.uranium.java.model.source.TryParameter
import norswap.uranium.java.model.source.TypedParameter
import norswap.uranium.java.model.source.UntypedParameter
import norswap.uranium.java.model.source.Variable
import norswap.utils.multimap.append
import java.util.ArrayDeque

// -------------------------------------------------------------------------------------------------

fun Context.register_java8_scopes_builder()
{
    propagator.register_java8_scopes_builder(ScopesBuilder(this))
}

// -------------------------------------------------------------------------------------------------

private fun Propagator.register_java8_scopes_builder (b: ScopesBuilder)
{
    add_visitor <AFile>             (b::visit_file)
    add_visitor <APackage>          (b::visit_pkg)
    add_visitor <Import>            (b::visit_import)
    add_visitor <TypeDecl>          (b::visit_class)
    add_visitor <CtorCall>          (b::visit_ctor_call)
    add_visitor <ConstructorDecl>   (b::visit_ctor)
    add_visitor <MethodDecl>        (b::visit_method)
    add_visitor <VarDecl>           (b::visit_var)
    add_visitor <TypeParam>         (b::visit_type_param)
    add_visitor <ASTLambda>         (b::visit_lambda)
    add_visitor <ASTBlock>          (b::visit_block)
    add_visitor <EnumConstant>      (b::visit_enum_constant)
    add_visitor <AnnotationElemDecl>(b::visit_annotation_elem)
    add_visitor <LabelledStmt>      (b::visit_label)
    add_visitor <CatchClause>       (b::visit_catch)
    add_visitor <TryStmt>           (b::visit_try)
    add_visitor <EnhancedFor>       (b::visit_enhanced_for)
    add_visitor <Identifier>        (b::visit_identifier)
}

// -------------------------------------------------------------------------------------------------

class ScopesBuilder (private val ctx: Context)
{
    // ---------------------------------------------------------------------------------------------

    private val default_package = Package(null)

    // ---------------------------------------------------------------------------------------------

    private lateinit var file: File

    private val classes = ArrayDeque<SourceClass>()
    private val indices = ArrayDeque<Int>()
    private val qindices = ArrayDeque<HashMap<String, Int>>()
    private val scopes  = ArrayDeque<Scope>()

    private val klass get()  = classes  .peek()
    private val scope get()  = scopes   .peek()
    private val qindex get() = qindices .peek()

    private var index
        get()  = indices.peek()
        set(x) { indices.pop() ; indices.push(x) }

    // ---------------------------------------------------------------------------------------------

    fun visit_file (node: AFile, start: Boolean)
    {
        // if (start) println(node.input.name) // TODO
        // println("file visited: $start") // TODO
        if (start) {
            file = File(node, default_package)
            scopes.push(file)
            ctx.propagator[node, "scope"] = file
        }
        else {
            scopes.pop()
        }
    }

    // ---------------------------------------------------------------------------------------------

    fun visit_pkg (node: APackage, start: Boolean)
    {
        // println("pkg visited: $start") // TODO
        if (!start) return
        file.pkg = Package(node)
    }

    // ---------------------------------------------------------------------------------------------

    fun visit_import (node: Import, start: Boolean)
    {
        // println("import visited: $start") // TODO
        if (!start) {

        }

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

    private fun push_class (klass: SourceClass)
    {
        classes  .push(klass)
        scopes   .push(klass)
        indices  .push(1)
        qindices .push(HashMap())
    }

    // ---------------------------------------------------------------------------------------------

    private fun pop_class()
    {
        classes  .pop()
        scopes   .pop()
        indices  .pop()
        qindices .pop()
    }

    // ---------------------------------------------------------------------------------------------

    fun visit_class (node: TypeDecl, start: Boolean)
    {
        if (!start) {
            pop_class()
            return
        }

        var index = 0
        if (scope !is Klass && scope !is File) {
            index = qindex.getOrPut(node.name) { 1 }
            qindex[node.name] = index + 1
        }

        val klass = SourceClass(node, scope, index)
        val skope = scope

        when (skope) {
            is File        -> {
                              skope.pkg.classes [node.name] = klass
                              skope.classes     [node.name] = klass
            }
            is SourceClass -> skope.klasses     [node.name] = klass
            is Block       -> skope.classes     [node.name] = klass
        }

        push_class(klass)
        ctx.resolver.add_source_class(klass)
    }

    // ---------------------------------------------------------------------------------------------

    fun visit_ctor_call (node: CtorCall, start: Boolean)
    {
        if (node.body == null) return
        if (!start) {
            pop_class()
            return
        }

        val klass = SourceClass(node, scope, index++)
        push_class(klass)
    }

    // ---------------------------------------------------------------------------------------------

    fun visit_ctor (node: ConstructorDecl, start: Boolean)
    {
        if (!start) {
            scopes.pop()
            return
        }

        val ctor = SourceConstructor(node, scope)
        klass.constructors += ctor
        scopes.push(ctor)

        node.tparams.forEach {
            ctor.type_params[it.name] = it
        }

        node.params.params.forEach {
            ctor.parameters[it.name] = TypedParameter(it)
        }
    }

    // ---------------------------------------------------------------------------------------------

    fun visit_method (node: MethodDecl, start: Boolean)
    {
        if (!start) {
            scopes.pop()
            return
        }

        val method = SourceMethod(node, klass)
        klass.methods.append(node.name, method)
        scopes.push(method)

        node.tparams.forEach {
            method.type_params[it.name] = SourceTypeParameter(it)
        }

        node.params.params.forEach {
            method.parameters[it.name] = TypedParameter(it)
        }
    }

    // ---------------------------------------------------------------------------------------------

    fun visit_var (node: VarDecl, start: Boolean)
    {
        if (!start) return

        val skope = scope
        when (skope) {
            is SourceClass ->
                for (decl in node.declarators)
                    skope.fields[decl.id.iden] = SourceField(node, decl)
            is Block ->
                for (decl in node.declarators)
                    skope.variables[decl.id.iden] = Variable(node, decl)
            is ControlFlow -> // basic for statement
                for (decl in node.declarators)
                    skope.variables[decl.id.iden] = Variable(node, decl)
        }
    }

    // ---------------------------------------------------------------------------------------------

    fun visit_type_param (node: TypeParam, start: Boolean)
    {
        klass.type_parameters[node.name] = SourceTypeParameter(node)
    }

    // ---------------------------------------------------------------------------------------------

    fun visit_lambda (node: norswap.lang.java8.ast.Lambda, start: Boolean)
    {
        if (!start) {
            scopes.pop()
            return
        }

        val lambda = Lambda(node, scope)

        when (node.params) {
            is FormalParameters ->
                node.params.params.forEach {
                    lambda.parameters[it.name] = TypedParameter(it)
                }
            is UntypedParameters ->
                node.params.params.forEach {
                    lambda.parameters[it] = UntypedParameter(it)
                }
        }

        scopes.push(lambda)
    }

    // ---------------------------------------------------------------------------------------------

    fun visit_block (node: norswap.lang.java8.ast.Block, start: Boolean)
    {
        if (!start) {
            scopes.pop()
            return
        }

        val block = Block(node, scope)
        scopes.push(block)
    }

    // ---------------------------------------------------------------------------------------------

    fun visit_enum_constant (node: EnumConstant, start: Boolean)
    {
        if (!start) {
            if (node.body != null) pop_class()
            return
        }

        klass.fields[node.name] = SourceEnumConstant(node)

        if (node.body != null)
            push_class(SourceClass(node, scope, index++))
    }

    // ---------------------------------------------------------------------------------------------

    fun visit_annotation_elem (node: AnnotationElemDecl, start: Boolean)
    {
        if (!start) return
        val elem = SourceAnnotationElement(node)
        klass.methods.append(node.name, elem)
    }

    // ---------------------------------------------------------------------------------------------

    fun visit_label (node: LabelledStmt, start: Boolean)
    {
        if (!start) return
        val skope = scope as Block
        skope.labels[node.label] = skope.node.stmts.indexOf(node)
    }

    // ---------------------------------------------------------------------------------------------

    fun visit_catch (node: CatchClause, start: Boolean)
    {
        if (!start) {
            scopes.pop()
            return
        }

        val flow = ControlFlow(node, scope)
        scopes.push(flow)
        flow.variables[node.id.iden] = CatchParameter(node)
    }

    // ---------------------------------------------------------------------------------------------

    fun visit_try (node: TryStmt, start: Boolean)
    {
        if (!start) {
            scopes.pop()
            return
        }

        val flow = ControlFlow(node, scope)
        scopes.push(flow)

        node.resources.forEach {
            flow.variables[it.id.iden] = TryParameter(it)
        }
    }

    // ---------------------------------------------------------------------------------------------

    fun visit_enhanced_for (node: EnhancedFor, start: Boolean)
    {
        if (!start) {
            scopes.pop()
            return
        }

        val flow = ControlFlow(node, scope)
        scopes.push(flow)
        flow.variables[node.id.iden] = ForParameter(node)
    }

    // ---------------------------------------------------------------------------------------------

    fun visit_identifier (node: Identifier, start: Boolean)
    {
        if (!start) return
        ctx.propagator[node, "scope"] = scope
    }

    // ---------------------------------------------------------------------------------------------
}