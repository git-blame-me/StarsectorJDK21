package data.agent;

import static net.bytebuddy.matcher.ElementMatchers.*;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.nio.ByteBuffer;
import java.nio.file.Files;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.agent.builder.AgentBuilder.InjectionStrategy;
import net.bytebuddy.agent.builder.AgentBuilder.RedefinitionStrategy;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.asm.AsmVisitorWrapper;
import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.field.FieldList;
import net.bytebuddy.description.method.MethodList;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.scaffold.TypeValidation;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.jar.asm.ClassVisitor;
import net.bytebuddy.jar.asm.commons.ClassRemapper;
import net.bytebuddy.jar.asm.commons.Remapper;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.pool.TypePool;

/**
 * Mods to allow starsector to run with jdk21
 * 
 *
 */
public class StarsectorAgentNewJDK {

	/**
	 * This loads up before the main starsector
	 * 
	 * @param args
	 * @param instrumentation
	 */
	public static void premain(String args, Instrumentation instrumentation) {

//		new ByteBuddy()
//			.redefine(com.fs.graphics.TextureLoader.class)
//			.method(named("o00000").and(takesArguments(java.nio.ByteBuffer.class)).and(takesArguments(String.class)))
//			.intercept(MethodDelegation.to(ReplaceOldCall.class))
//			.make().load(ReplaceOldCall.class.getClassLoader(), ClassLoadingStrategy.Default.INJECTION).getLoaded();
//			;

//		new AgentBuilder.Default().with(RedefinitionStrategy.DISABLED)
//				.with(AgentBuilder.Listener.StreamWriting.toSystemOut().withTransformationsOnly())
//				.disableClassFormatChanges().type(nameStartsWith("com.fs.graphics.TextureLoader"))
//				.transform((builder, typeDescription, classLoader, module, protectionDomain) -> {
////						if (classLoader != null) {
////							System.out.println("Classloader: " + classLoader.getName());
////						}
//					return builder.method(named("o00000").and(ElementMatchers.isStatic())
//					// .and(takesArguments(java.nio.ByteBuffer.class))
//					// .and(takesArguments(String.class))
//					).intercept(MethodDelegation.to(CleanerFix.class));
//				}).installOn(instrumentation);
		
		ByteBuddy bud = new ByteBuddy().with(TypeValidation.DISABLED);
		
		
		//i guess better than just disabling the method altogether
		new AgentBuilder.Default().with(bud)
		.with(AgentBuilder.Listener.StreamWriting.toSystemError().withTransformationsOnly())
		.with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
		.type(named("com.fs.graphics.TextureLoader"))
		.transform((builder, typeDescription, classLoader, module, protectionDomain) -> builder
				.visit(new AsmVisitorWrapper.AbstractBase() {
					public ClassVisitor wrap(TypeDescription instrumentedType, ClassVisitor classVisitor,
							Implementation.Context implementationContext, TypePool typePool,
							FieldList<FieldDescription.InDefinedShape> fields, MethodList<?> methods,
							int writerFlags, int readerFlags) {
						return new ClassRemapper(classVisitor,
								new Remapper() {
									@Override
									public String map(String typeName) {
										if ("sun/misc/Cleaner"
												.equals(typeName)) {
											return "jdk/internal/ref/Cleaner";
										}
										return typeName;
									}
								});
					}
				}))
		.installOn(instrumentation);

		final File tempFolder;

		try {
			tempFolder = Files.createTempDirectory("agent-bootstrap").toFile();
		} catch (Exception e) {
			System.err.println("Cannot create temp folder for bootstrap class instrumentation");
			e.printStackTrace(System.err);
			return;
		}

		//fix for Thread.stop()
		//in 20 or 21 Thread.stop() throws an exception
		new AgentBuilder.Default().disableClassFormatChanges()
				.with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
				// Make sure we see helpful logs
				.with(AgentBuilder.RedefinitionStrategy.Listener.StreamWriting.toSystemError())
				.with(AgentBuilder.Listener.StreamWriting.toSystemError().withTransformationsOnly())
				.with(AgentBuilder.InstallationListener.StreamWriting.toSystemError())
				.with(AgentBuilder.InitializationStrategy.NoOp.INSTANCE)
				.with(AgentBuilder.TypeStrategy.Default.REDEFINE)
				.with(new InjectionStrategy.UsingInstrumentation(instrumentation, tempFolder))
				// Ignore Byte Buddy and JDK classes we are not interested in
				.ignore(nameStartsWith("net.bytebuddy.").or(nameStartsWith("com.fs")))
				.type(is(Thread.class))
				.transform((builder, type, classLoader, module, domain) -> builder
						.visit(Advice.to(ThreadFix.class).on(named("stop").and(isMethod()))))
				.installOn(instrumentation);

		//unneeded fix for updated xstream lib, new lib was not required
		//this just disabled the default security on the new lib
		// com.thoughtworks.xstream.security.NoTypePermission
//		new AgentBuilder.Default().with(RedefinitionStrategy.DISABLED)
//				.with(AgentBuilder.Listener.StreamWriting.toSystemOut().withTransformationsOnly())
//				.disableClassFormatChanges().type(named("com.thoughtworks.xstream.security.NoTypePermission"))
//				.transform((builder, typeDescription, classLoader, module, protectionDomain) -> {
//					return builder.method(named("allows")
//					// .and(takesArguments(java.nio.ByteBuffer.class))
//					// .and(takesArguments(String.class))
//					).intercept(MethodDelegation.to(XStreamFix.class));
//				}).installOn(instrumentation);

		// com/sun/xml/internal/txw2/output/IndentingXMLStreamWriter
		// to
		// com/sun/xml/txw2/output/IndentingXMLStreamWriter
		// in
		// com.fs.starfarer.campaign.save.CampaignGameManager

		new AgentBuilder.Default().with(bud)
				.with(AgentBuilder.Listener.StreamWriting.toSystemError().withTransformationsOnly())
				.with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
				.type(nameStartsWith("com.fs.starfarer.campaign.save.CampaignGameManager$5"))
				.transform((builder, typeDescription, classLoader, module, protectionDomain) -> builder
						.visit(new AsmVisitorWrapper.AbstractBase() {
							public ClassVisitor wrap(TypeDescription instrumentedType, ClassVisitor classVisitor,
									Implementation.Context implementationContext, TypePool typePool,
									FieldList<FieldDescription.InDefinedShape> fields, MethodList<?> methods,
									int writerFlags, int readerFlags) {
								//System.out.println("remapping CampaignGameManager$5");
								return new ClassRemapper(classVisitor,
										new Remapper() {
											@Override
											public String map(String typeName) {
												//System.out.println("remapping typeName");
												if ("com/sun/xml/internal/txw2/output/IndentingXMLStreamWriter"
														.equals(typeName)) {
													//System.out.println("remapping success");
													return "com/sun/xml/txw2/output/IndentingXMLStreamWriter";
												}
												return typeName;
											}
										});
							}
						}))
				.installOn(instrumentation);

		

	}

	/**
	 * this does the actual replacement of the returned value
	 * 
	 *
	 */
	public static class CleanerFix {

		public static void intercept(ByteBuffer arg0, String arg1) {
			// System.out.println("CleanerFix Intercepted");
			return;
		}
	}

	/**
	 * this does the actual replacement of the returned value
	 * 
	 *
	 */
	public static class ThreadFix {
		@Advice.OnMethodEnter(skipOn = Advice.OnDefaultValue.class)
		public static boolean intercept() {
			// System.out.println("ThreadFix Intercepted");
			return false;
		}
	}
}