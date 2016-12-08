package org.robolectric.res;

import org.jetbrains.annotations.NotNull;
import org.robolectric.res.builder.XmlBlock;

import java.io.IOException;
import java.io.InputStream;

// TODO: Give me a better name
abstract class XResourceLoader extends ResourceLoader {
  final ResBunch data = new ResBunch();
  final ResBundle rawResources = new ResBundle();
  
  private final ResourceIndex resourceIndex;
  private boolean isInitialized = false;

  XResourceLoader(ResourceIndex resourceIndex) {
    this.resourceIndex = resourceIndex;
  }

  abstract void doInitialize();

  synchronized void initialize() {
    if (isInitialized) return;
    doInitialize();
    isInitialized = true;

    makeImmutable();
  }

  private void makeImmutable() {
    data.makeImmutable();

    rawResources.makeImmutable();
  }

  public TypedResource getValue(@NotNull ResName resName, String qualifiers) {
    initialize();
    return data.get(resName, qualifiers);
  }

  @Override
  public XmlBlock getXml(ResName resName, String qualifiers) {
    initialize();
    TypedResource typedResource = data.get(resName, qualifiers);
    if (typedResource instanceof FileTypedResource) {
      FileTypedResource fileTypedResource = (FileTypedResource) typedResource;
      if (fileTypedResource.getFsFile().getPath().endsWith("xml")) {
        return XmlBlock.create(fileTypedResource.getFsFile(),
            fileTypedResource.getXmlContext().resourcePath.getPackageName());
      }
    }
    return null;
  }

  @Override
  public InputStream getRawValue(ResName resName) {
    initialize();

    TypedResource typedResource = rawResources.get(resName, "");
    FsFile file = typedResource == null ? null : (FsFile) typedResource.getData();
    try {
      return file == null ? null : file.getInputStream();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public ResourceIndex getResourceIndex() {
    return resourceIndex;
  }

  @Override
  public void receive(Visitor visitor) {
    initialize();
    data.receive(visitor);
  }
}
