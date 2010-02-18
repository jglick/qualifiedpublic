package qualifiedpublic;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.AbstractTypeProcessor;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;
import java.util.Arrays;
import java.util.Iterator;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic.Kind;
@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
// http://stackoverflow.com/questions/1066555/discover-the-class-of-a-methodinvocation-in-the-annotation-processor-for-java
public class PublicProcessor extends AbstractTypeProcessor {
    static {
        // XXX work around fact that URLClassLoader.close is called before typeProcess:
        TreePathScannerImpl.class.hashCode();
        Public.class.hashCode();
    }
    public @Override void typeProcess(TypeElement te, TreePath root) {
//        for (Element e : roundEnv.getElementsAnnotatedWith(Public.class)) {
//            // XXX verify that it is marked public
//        }
        new TreePathScannerImpl(root, Trees.instance(processingEnv)).scan(root, null);
    }
    private class TreePathScannerImpl extends TreePathScanner<Void,Void> {
        private final TreePath root;
        private final Trees trees;
        TreePathScannerImpl(TreePath root, Trees trees) {
            this.root = root;
            this.trees = trees;
        }
        public @Override Void visitMemberSelect(MemberSelectTree node, Void p) {
            Name member = node.getIdentifier();
            TreePath expr = TreePath.getPath(root, node.getExpression());
            TypeMirror declarer = trees.getTypeMirror(expr);
            TypeElement clazz = (TypeElement) processingEnv.getTypeUtils().asElement(declarer);
            for (Element enclosed : clazz.getEnclosedElements()) {
                if (enclosed.getSimpleName().equals(member)) {
                    // XXX would also want to match signature for method overloads
                    Public pub = enclosed.getAnnotation(Public.class);
                    if (pub != null) {
                        if (!Arrays.asList(pub.value()).contains(fqn(getCurrentPath()))) {
                            processingEnv.getMessager().printMessage(Kind.ERROR, "Cannot access " + declarer + "." + member, elementOf(getCurrentPath()));
                        }
                    }
                }
            }
            return super.visitMemberSelect(node, p);
        }
        // XXX implicit this, super
        private String fqn(TreePath path) {
            String fqn = null;
            Iterator<Tree> it = path.iterator();
            while (it.hasNext()) {
                Tree t = it.next();
                if (t.getKind() == Tree.Kind.CLASS) {
                    String simpleName = ((ClassTree) t).getSimpleName().toString();
                    if (fqn == null) {
                        fqn = simpleName;
                    } else {
                        fqn = simpleName + "." + fqn;
                    }
                } else if (t.getKind() == Tree.Kind.COMPILATION_UNIT) {
                    fqn = ((CompilationUnitTree) t).getPackageName().toString() + "." + fqn;
                }
            }
            return fqn;
        }
        private Element elementOf(TreePath path) {
            Element e = trees.getElement(path);
            if (e != null) {
                return e;
            } else {
                return elementOf(path.getParentPath());
            }
        }
    }
}
