package org.mitre.synthea.helpers;

public final class Constants {

  public static final String GENERATOR_MAX_GENERATOR_THREADS = "generator.num_generator_threads";

  public static final String EXPORTER_FHIR_DSTU2_TARGET_FHIRSVR_BASEURL = "exporter.fhir_dstu2.target_server_base_url";
  public static final String EXPORTER_FHIR_STU3_TARGET_FHIRSVR_BASEURL = "exporter.fhir_stu3.target_server_base_url";
  public static final String EXPORTER_FHIR_R4_TARGET_FHIRSVR_BASEURL = "exporter.fhir_r4.target_server_base_url";

  public static final String EXPORTER_FHIR_DSTU2_EXCLUDE_ORG_PRAC_RESOURCES = "exporter.fhir_dstu2.exclude_organization_and_practitioner_resources";
  public static final String EXPORTER_FHIR_STU3_EXCLUDE_ORG_PRAC_RESOURCES = "exporter.fhir_stu3.exclude_organization_and_practitioner_resources";
  public static final String EXPORTER_FHIR_R4_EXCLUDE_ORG_PRAC_RESOURCES = "exporter.fhir_r4.exclude_organization_and_practitioner_resources";

  public static final String EXPORTER_FHIR_ALLVERSIONS_COMPRESS_DATA = "exporter.fhir.allversions.compress_data";
  public static final String EXPORTER_FHIR_TXBUNDLE_USE_SEARCH_URLS_IN_REFS = "exporter.fhir.transaction_bundle.use_search_urls_in_refs";

  public static final String EXPORT_AWS_S3_EXPORT_ENABLED = "exporter.aws.s3.export_enabled";
  public static final String EXPORT_AWS_S3_BUCKET_NAME = "exporter.aws.s3.bucket_name";
  public static final String EXPORT_AWS_S3_BUCKET_BASE_PATH = "exporter.aws.s3.bucket_base_path";
  public static final String EXPORT_AWS_S3_ACCESS_KEY = "exporter.aws.s3.aws_access_key";
  public static final String EXPORT_AWS_S3_SECRET_KEY = "exporter.aws.s3.aws_secret_key";

  private Constants() {
    // noop
  }
}
