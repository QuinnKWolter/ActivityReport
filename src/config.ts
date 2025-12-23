import * as dotenv from 'dotenv';

dotenv.config();

export interface DatabaseConfig {
  host: string;
  port: number;
  user: string;
  password: string;
  database: string;
  ssh?: {
    host: string;
    username: string;
    password?: string;
    privateKey?: string;
    port?: number;
  };
}

export interface AppConfig {
  um2: DatabaseConfig;
  aggregate: DatabaseConfig;
  port: number;
  delimiter: string;
}

export function loadConfig(): AppConfig {
  // Check if SSH tunneling is needed
  const useSSH = process.env.SSH_HOST !== undefined;
  const sshHost = process.env.SSH_HOST;
  const sshUser = process.env.SSH_USER;
  const sshPassword = process.env.SSH_PASSWORD;
  const sshPort = parseInt(process.env.SSH_PORT || '22', 10);

  const config = {
    um2: {
      host: useSSH ? '127.0.0.1' : (process.env.UM2_DB_HOST || 'localhost'),
      port: parseInt(process.env.UM2_DB_PORT || '3306', 10),
      user: process.env.UM2_DB_USER || 'root',
      password: process.env.UM2_DB_PASSWORD || '',
      database: process.env.UM2_DB_NAME || 'um2',
      ...(useSSH && sshHost && sshUser ? {
        ssh: {
          host: sshHost,
          username: sshUser,
          password: sshPassword,
          port: sshPort,
        }
      } : {}),
    },
    aggregate: {
      host: useSSH ? '127.0.0.1' : (process.env.AGGREGATE_DB_HOST || 'localhost'),
      port: parseInt(process.env.AGGREGATE_DB_PORT || '3306', 10),
      user: process.env.AGGREGATE_DB_USER || 'root',
      password: process.env.AGGREGATE_DB_PASSWORD || '',
      database: process.env.AGGREGATE_DB_NAME || 'aggregate',
      ...(useSSH && sshHost && sshUser ? {
        ssh: {
          host: sshHost,
          username: sshUser,
          password: sshPassword,
          port: sshPort,
        }
      } : {}),
    },
    port: (() => {
      const port = process.env.PORT ? parseInt(process.env.PORT, 10) : 3000;
      // Ensure port is valid and not a database port
      if (isNaN(port) || port < 1024 || port > 65535) {
        console.warn(`Invalid PORT value: ${process.env.PORT}, defaulting to 3000`);
        return 3000;
      }
      return port;
    })(),
    delimiter: process.env.DELIMITER || ',',
  };

  // Validate server port (shouldn't be a database port)
  if (config.port === 3306 || config.port === config.um2.port || config.port === config.aggregate.port) {
    console.error('\n❌ ERROR: Server port cannot be the same as database port!');
    console.error(`   Current PORT=${config.port} conflicts with database ports.`);
    console.error('   Please set PORT to a different value (e.g., 3000) in your .env file.\n');
    process.exit(1);
  }

  // Log configuration (without passwords) for debugging
  console.log('Database Configuration:');
  if (config.um2.ssh) {
    console.log(`  UM2: ${config.um2.user}@${config.um2.host}:${config.um2.port}/${config.um2.database} (via SSH: ${config.um2.ssh.username}@${config.um2.ssh.host})`);
  } else {
    console.log(`  UM2: ${config.um2.user}@${config.um2.host}:${config.um2.port}/${config.um2.database}`);
  }
  if (config.aggregate.ssh) {
    console.log(`  Aggregate: ${config.aggregate.user}@${config.aggregate.host}:${config.aggregate.port}/${config.aggregate.database} (via SSH: ${config.aggregate.ssh.username}@${config.aggregate.ssh.host})`);
  } else {
    console.log(`  Aggregate: ${config.aggregate.user}@${config.aggregate.host}:${config.aggregate.port}/${config.aggregate.database}`);
  }
  console.log(`  Server Port: ${config.port}`);

  // Check if .env file exists
  if (!process.env.UM2_DB_USER && !process.env.AGGREGATE_DB_USER) {
    console.warn('\n⚠️  WARNING: No .env file found or environment variables not set!');
    console.warn('   Please create a .env file with your database credentials.');
    console.warn('   See .env.example for reference.\n');
  }

  return config;
}

export const config = loadConfig();

