package org.mitre.synthea.export;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.BundleEntryRequestComponent;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.Bundle.HTTPVerb;
import org.hl7.fhir.r4.model.IntegerType;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.Resource;
import org.mitre.synthea.helpers.Config;
import org.mitre.synthea.helpers.Constants;
import org.mitre.synthea.helpers.RandomNumberGenerator;
import org.mitre.synthea.helpers.Utilities;
import org.mitre.synthea.world.agents.Clinician;
import org.mitre.synthea.world.agents.Provider;

import com.google.common.collect.Table;

public abstract class FhirPractitionerExporterR4 {

  private static final String EXTENSION_URI = 
      "http://synthetichealth.github.io/synthea/utilization-encounters-extension";

  /**
   * Export the practitioner in FHIR R4 format.
   */
  public static void export(RandomNumberGenerator rand, long stop) {
    if (Config.getAsBoolean("exporter.practitioner.fhir.export")) {

      Bundle bundle = new Bundle();
      if (Config.getAsBoolean("exporter.fhir.transaction_bundle")) {
        bundle.setType(BundleType.BATCH);
      } else {
        bundle.setType(BundleType.COLLECTION);
      }
      for (Provider h : Provider.getProviderList()) {
        // filter - exports only those hospitals in use

        Table<Integer, String, AtomicInteger> utilization = h.getUtilization();
        int totalEncounters = utilization.column(Provider.ENCOUNTERS).values().stream()
            .mapToInt(ai -> ai.get()).sum();
        if (totalEncounters > 0) {
          Map<String, ArrayList<Clinician>> clinicians = h.clinicianMap;
          for (String specialty : clinicians.keySet()) {
            ArrayList<Clinician> docs = clinicians.get(specialty);
            for (Clinician doc : docs) {
              if (doc.getEncounterCount() > 0) {
                BundleEntryComponent entry = FhirR4.practitioner(rand, bundle, doc);
                Practitioner practitioner = (Practitioner) entry.getResource();
                practitioner.addExtension()
                  .setUrl(EXTENSION_URI)
                  .setValue(new IntegerType(doc.getEncounterCount()));
              }
            }
          }
        }
      }

      if (Boolean.parseBoolean(Config.get(Constants.EXPORTER_FHIR_R4_EXCLUDE_ORG_PRAC_RESOURCES))) {
        for (BundleEntryComponent bec : bundle.getEntry()) {
          Resource r = bec.getResource();
          bec.setFullUrl("http://synthea-dummy/fhir/" + r.getResourceType().name() + "/" + r.getIdElement().getIdPart());
          BundleEntryRequestComponent req = bec.getRequest();
          req.setMethod(HTTPVerb.PUT);
          req.setUrl(r.getResourceType().name() + "/" + r.getIdElement().getIdPart());
        }
      }

      String bundleJson = FhirR4.getContext().newJsonParser().setPrettyPrint(true)
          .encodeResourceToString(bundle);

      if (!StringUtils.isBlank(Config.get(Constants.EXPORTER_FHIR_R4_TARGET_FHIRSVR_BASEURL))) {
        try {
          Utilities.sendFhirJsonToUrl(bundleJson, Config.get(Constants.EXPORTER_FHIR_R4_TARGET_FHIRSVR_BASEURL));
        } catch (Exception e) {
          e.printStackTrace();
        }
      } else {
        // get output folder
        List<String> folders = new ArrayList<>();
        folders.add("fhir");
        String baseDirectory = Config.get("exporter.baseDirectory");
        File f = Paths.get(baseDirectory, folders.toArray(new String[0])).toFile();
        f.mkdirs();
        Path outFilePath = f.toPath().resolve("practitionerInformation" + stop + ".json");

        if (Boolean.valueOf(Config.get(Constants.EXPORTER_FHIR_ALLVERSIONS_COMPRESS_DATA))) {
          File zipFile = Utilities.compressToZip(outFilePath.toFile(), bundleJson);
          if (Boolean.valueOf(Config.get(Constants.EXPORT_AWS_S3_EXPORT_ENABLED))) {
            Utilities.uploadToAwsS3(zipFile,
              Config.get(Constants.EXPORT_AWS_S3_BUCKET_NAME),
              Config.get(Constants.EXPORT_AWS_S3_BUCKET_BASE_PATH),
              Config.get(Constants.EXPORT_AWS_S3_ACCESS_KEY),
              Config.get(Constants.EXPORT_AWS_S3_SECRET_KEY));
          }
        } else {
          try {
            Files.write(outFilePath, Collections.singleton(bundleJson), StandardOpenOption.CREATE_NEW);
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }
    }
  }
}