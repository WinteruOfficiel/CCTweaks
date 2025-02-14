package org.squiddev.cctweaks.core.asm;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import net.minecraft.launchwrapper.IClassTransformer;
import org.apache.logging.log4j.Level;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.TraceClassVisitor;
import org.squiddev.cctweaks.core.Config;
import org.squiddev.cctweaks.core.utils.DebugLogger;
import org.squiddev.cctweaks.integration.multipart.MultipartIntegration;
import org.squiddev.cctweaks.lua.asm.CustomChain;
import org.squiddev.cctweaks.lua.asm.Tweaks;
import org.squiddev.patcher.Logger;
import org.squiddev.patcher.transformer.ClassMerger;
import org.squiddev.patcher.transformer.ClassReplacer;
import org.squiddev.patcher.transformer.IPatcher;
import org.squiddev.patcher.transformer.ISource;

import java.io.*;

public class ASMTransformer implements IClassTransformer {
	private final CustomChain patches = new CustomChain();

	protected void add(Object[] patchers) {
		for (Object patcher : patchers) {
			if (patcher instanceof IPatcher) patches.add((IPatcher) patcher);
			if (patcher instanceof ISource) patches.add((ISource) patcher);
		}
	}

	public ASMTransformer() {
		/*
			TODO: Look into moving some rewrites into compile-time processing instead.
			This probably includes *_Rewrite as well as many of the binary handlers as they only exist
			because they need to stub classes that we patch anyway.
		 */
		Tweaks.setup(patches);

		add(new Object[]{
			// General stuff
			new ClassReplacer(
				"dan200.computercraft.shared.turtle.core.TurtleRefuelCommand",
				"org.squiddev.cctweaks.core.patch.TurtleRefuelCommand_Rewrite"
			),
			new ClassMerger(
				"dan200.computercraft.shared.peripheral.common.BlockPeripheral",
				"org.squiddev.cctweaks.core.patch.BlockPeripheral_Patch"
			),
			new ClassMerger(
				"dan200.computercraft.shared.turtle.core.TurtleBrain",
				"org.squiddev.cctweaks.core.patch.TurtleBrain_Patch"
			),
			new DisableTurtleCommand(),
			new PatchTurtleRenderer(),
			new TurtleBrainAlsoPeripheral(),

			// Networking
			new ClassMerger(
				"dan200.computercraft.shared.peripheral.common.BlockCable",
				"org.squiddev.cctweaks.core.patch.BlockCable_Patch"
			),
			new ClassMerger(
				"dan200.computercraft.shared.peripheral.modem.TileCable",
				"org.squiddev.cctweaks.core.patch.TileCable_Patch"
			),
			new ClassMerger(
				"dan200.computercraft.shared.peripheral.common.ItemCable",
				"org.squiddev.cctweaks.core.patch.ItemCable_Patch"
			) {
				/**
				 * The item cable patch just implements multipart support.
				 * If multipart is not loaded, then we cannot patch this without errors
				 */
				@Override
				public boolean matches(String className) {
					return super.matches(className) && Loader.isModLoaded(MultipartIntegration.NAME) && Config.Integration.cbMultipart;
				}
			},
			new ClassMerger(
				"dan200.computercraft.client.proxy.ComputerCraftProxyClient$CableBlockRenderingHandler",
				"org.squiddev.cctweaks.core.patch.CableBlockRenderingHandler_Patch"
			),
			new ClassMerger(
				"dan200.computercraft.core.apis.PeripheralAPI",
				"org.squiddev.cctweaks.core.patch.PeripheralAPI_Patch"
			),

			// Open peripheral
			new ClassMerger(
				"openperipheral.addons.peripheralproxy.WrappedPeripheral",
				"org.squiddev.cctweaks.core.patch.op.PeripheralProxy_Patch"
			),

			// OP Patches: targeted peripherals & network environment
			new PatchOpenPeripheralAdapter(),
			new PatchOpenModule(),

			// Targeted peripherals
			new ClassMerger(
				"dan200.computercraft.shared.computer.blocks.ComputerPeripheral",
				"org.squiddev.cctweaks.core.patch.targeted.ComputerPeripheral_Patch"
			),
			new ClassMerger(
				"dan200.computercraft.shared.peripheral.diskdrive.DiskDrivePeripheral",
				"org.squiddev.cctweaks.core.patch.targeted.DiskDrivePeripheral_Patch"
			),
			new ClassMerger(
				"dan200.computercraft.shared.peripheral.printer.PrinterPeripheral",
				"org.squiddev.cctweaks.core.patch.targeted.PrinterPeripheral_Patch"
			),
		});

		patches.finalise();

		// Patch the logger instance
		Logger.instance = new Logger() {
			@Override
			public void doDebug(String message) {
				DebugLogger.debug(message);
			}

			@Override
			public void doError(String message, Throwable e) {
				DebugLogger.error(message, e);
			}

			@Override
			public void doWarn(String message) {
				DebugLogger.warn(message);
			}
		};
	}

	private boolean loadedCC = false;
	private String[] message;

	private void checkCC() {
		loadedCC = true;
		for (ModContainer x : Loader.instance().getModList()) {
			if (x.getName().equals("ComputerCraft") && !x.getVersion().equals("${cc_version}")) {
				message = new String[]{
					"CCTweaks ${mod_version} was tested against ComputerCraft ${cc_version} but is running against " + x.getVersion() + ".",
					"Some CCTweaks/ComputerCraft features may not work correctly - please check CCTweaks for updates.",
					"If you encounter issues then try to reproduce without CCTweaks installed, then report to the appropriate mod author.",
				};
				DebugLogger.major(Level.WARN, message);
			}
		}
	}

	@Override
	public byte[] transform(String className, String s2, byte[] bytes) {
		if (!loadedCC && className.startsWith("dan200.computercraft.")) checkCC();

		try {
			byte[] rewritten = patches.transform(className, bytes);
			if (rewritten != bytes) writeDump(className, rewritten);
			return rewritten;
		} catch (Exception e) {
			String contents = "Cannot patch " + className + ", falling back to default";
			if (message != null) {
				DebugLogger.beginMajor(Level.ERROR);
				DebugLogger.error(contents, e);
				for (String line : message) {
					DebugLogger.error(line);
				}
				DebugLogger.endMajor(Level.ERROR);
			} else {
				DebugLogger.error(contents, e);
			}

			return bytes;
		}
	}

	public void dump(String className, byte[] bytes) {
		StringWriter writer = new StringWriter();
		new ClassReader(bytes).accept(new TraceClassVisitor(new PrintWriter(writer)), 0);
		DebugLogger.debug("Dump for " + className + "\n" + writer.toString());
	}

	public void writeDump(String className, byte[] bytes) {
		if (org.squiddev.cctweaks.lua.Config.Testing.dumpAsm) {
			File file = new File(TweaksLoadingPlugin.dump, className.replace('.', '/') + ".class");
			File directory = file.getParentFile();
			if (directory.exists() || directory.mkdirs()) {
				try {
					OutputStream stream = new FileOutputStream(file);
					try {
						stream.write(bytes);
					} catch (IOException e) {
						DebugLogger.error("Cannot write " + file, e);
					} finally {
						stream.close();
					}
				} catch (FileNotFoundException e) {
					DebugLogger.error("Cannot write " + file, e);
				} catch (IOException e) {
					DebugLogger.error("Cannot write " + file, e);
				}
			} else {
				DebugLogger.error("Cannot create folder for " + file);
			}
		}
	}
}
