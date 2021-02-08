package org.gr.testing.proxy;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

import com.google.gson.Gson;

import io.javalin.Javalin;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.filters.RequestFilter;
import net.lightbody.bmp.mitm.PemFileCertificateSource;
import net.lightbody.bmp.mitm.RootCertificateGenerator;
import net.lightbody.bmp.mitm.manager.ImpersonatingMitmManager;
import net.lightbody.bmp.proxy.CaptureType;
import net.lightbody.bmp.util.HttpMessageContents;
import net.lightbody.bmp.util.HttpMessageInfo;

public class Main {

	public static void main(String[] args) throws UnknownHostException, SocketException {

		final String baseDir = args.length > 0 ? args[0] : ".";
		final File keyFile = new File(baseDir, "demo.key");
		final File certFile = new File(baseDir, "demo.cer");
		final String privateKeyPwd = "demo123";
		final Gson gson = new Gson();
		final int proxyServicePort = 8888;
		final int restServicePort = 7000;

		final BrowserMobProxy proxy = new BrowserMobProxyServer();

		// checks if keys exist and generate certificates if required
		{
			if (!certFile.exists() || !keyFile.exists()) {
				RootCertificateGenerator rootCertificateGenerator = RootCertificateGenerator.builder().build();
				rootCertificateGenerator.saveRootCertificateAsPemFile(certFile);
				rootCertificateGenerator.savePrivateKeyAsPemFile(keyFile, privateKeyPwd);
				System.out.println("New certificate generated");
			}
		}

		// start the browser mob proxy
		{
			PemFileCertificateSource rootCertificateGenerator = new PemFileCertificateSource(certFile, keyFile,
					privateKeyPwd);

			ImpersonatingMitmManager mitmManager = ImpersonatingMitmManager.builder()
					.rootCertificateSource(rootCertificateGenerator).trustAllServers(true).build();

			proxy.setMitmManager(mitmManager);
			proxy.setHarCaptureTypes(CaptureType.REQUEST_CONTENT, CaptureType.REQUEST_COOKIES,
					CaptureType.REQUEST_HEADERS, CaptureType.RESPONSE_CONTENT, CaptureType.RESPONSE_COOKIES,
					CaptureType.RESPONSE_HEADERS);
			proxy.addRequestFilter(new RequestFilter() {
				public HttpResponse filterRequest(HttpRequest request, HttpMessageContents contents,
						HttpMessageInfo messageInfo) {
					String url = messageInfo.getOriginalUrl().toLowerCase();
					System.out.println("Serving request: " + url);
					return null;
				}
			});
			proxy.newHar();
			proxy.start(proxyServicePort);
			System.out.println("Proxy started at: " + proxyServicePort);

		}

		// start the REST service for controlling browser-mob
		{
			Javalin app = Javalin.create().start(restServicePort);
			app.get("/", ctx -> ctx.result("Hello World"));
			app.get("/certificate", ctx -> {
				InputStream targetStream = new FileInputStream(certFile);
				ctx.header("Content-Disposition", "attachment; filename=\"" + certFile.getName() + "\"");
				ctx.result(targetStream);
			});
			app.get("/har", ctx -> {
				ctx.header("Content-Disposition", "attachment; filename=\"out.har\"");
				ctx.result(gson.toJson(proxy.getHar()));
			});
			app.get("/view", ctx -> {
				InputStream targetStream = new FileInputStream(certFile);
				ctx.result(targetStream);
			});
			app.get("/newHar", ctx -> {
				proxy.newHar();
				ctx.result("OK");
			});
			app.get("/newPage", ctx -> {
				String newPageName = ctx.queryParam("page", "newPage-" + System.currentTimeMillis());
				proxy.newPage();
				ctx.result(newPageName);
			});
			System.out.println("REST service started at: " + restServicePort);
		}
		{
			NetworkInterface.getNetworkInterfaces().asIterator().forEachRemaining(add -> {
				add.getInterfaceAddresses().forEach(a -> {
					System.out.println("Listening on: " + a);
				});
			});
		}

	}

}
