// Generates a fern-nursery-config.json which can be used to
// deploy Nursery to Fern's environment.
// If you are an external consumer, this is irrelevant!

import {
  EnvironmentInfo,
  EnvironmentType,
  Environments,
} from "@fern-fern/fern-cloud-sdk/api/resources/environments";
import axios from "axios";
import { writeFileSync } from "fs";
import { NurseryInfraConfig } from "../config";

interface EnvironmentVariables {
  awsAccountId: string;
  postgresUsername: string;
  postgresHost: string;
  postgresPassword: string;
}

main();

async function main() {
  const environmentType = process.argv[2];
  const envVars: EnvironmentVariables = {
    awsAccountId: getEnvVarValueOrThrow("AWS_ACCOUNT_ID"),
    postgresUsername: getEnvVarValueOrThrow("POSTGRES_USERNAME"),
    postgresHost: getEnvVarValueOrThrow("POSTGRES_HOST"),
    postgresPassword: getEnvVarValueOrThrow("POSTGRES_PASSWORD"),
  };
  const environmentInfo = await getEnvironments(environmentType);
  if (environmentInfo == null) {
    throw new Error("Unexpected error: environmentInfo is undefined");
  }

  const config: NurseryInfraConfig = {
    stackName: `nursery-${environmentType}`,
    accountNumber: envVars.awsAccountId,
    region: "us-east-1",
    vpcId: environmentInfo.vpcId,
    ecsClusterName: environmentInfo.ecsInfo.clusterName,
    logGroupName: environmentInfo.logGroupInfo.logGroupName,
    route53HostedZoneId: environmentInfo.route53Info.hostedZoneId,
    route53HostedZoneName: environmentInfo.route53Info.hostedZoneName,
    domainName: getServiceDomainName(environmentType, environmentInfo),
    certificateArn: environmentInfo.route53Info.certificateArn,
    databaseConfig: {
      postgresHostname: envVars.postgresHost,
      postgresUsername: envVars.postgresUsername,
      postgresPassword: envVars.postgresPassword,
    },
    cloudmapConfig: {
      cloudmapArn: environmentInfo.cloudMapNamespaceInfo.namespaceArn,
      cloudmapId: environmentInfo.cloudMapNamespaceInfo.namespaceId,
      cloudmapName: environmentInfo.cloudMapNamespaceInfo.namespaceName,
    },
  };
  writeFileSync(`${environmentType}-fern.config.json`, JSON.stringify(config));
}

function getServiceDomainName(
  environmentType: string,
  environmentInfo: EnvironmentInfo
) {
  if (environmentType === "prod") {
    return `nursery.${environmentInfo.route53Info.hostedZoneName}`;
  }
  return `nursery-${environmentType.toLowerCase()}.${
    environmentInfo.route53Info.hostedZoneName
  }`;
}

function getEnvVarValueOrThrow(environmentVariableName: string): string {
  const val = process.env[environmentVariableName];
  if (val != null) {
    return val;
  }
  throw new Error("Missing environment variable: " + environmentVariableName);
}

async function getEnvironments(environment: string): Promise<EnvironmentInfo | undefined> {
  const response = await axios(
    "https://raw.githubusercontent.com/fern-api/fern-cloud/main/env-scoped-resources/environments.json",
    {
      method: "GET",
      headers: {
        Authorization: "Bearer " + process.env["GITHUB_TOKEN"],
      },
    }
  );
  const environments = response.data as Environments;
  if (environment === "prod") {
    return environments[EnvironmentType.Prod];
  } else if (environment === "dev") {
    return environments[EnvironmentType.Dev];
  } else if (environment === "dev2") {
    return environments[EnvironmentType.Dev2];
  }
  throw new Error("Encountered unknown environment: " + environment);
}
