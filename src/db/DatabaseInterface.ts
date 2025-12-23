import mysql, { Pool, PoolConnection } from 'mysql2/promise';
import { Client, ClientChannel } from 'ssh2';
import { createServer, Server } from 'net';
import { DatabaseConfig } from '../config';

export abstract class DatabaseInterface {
  protected pool!: Pool; // Will be initialized in createPool
  protected sshClient: Client | null = null;
  protected sshConfig: DatabaseConfig['ssh'] | null = null;
  protected localServer: Server | null = null;
  protected localPort: number | null = null;
  private config: DatabaseConfig;

  constructor(config: DatabaseConfig) {
    this.config = config;
    this.sshConfig = config.ssh || null;

    if (this.sshConfig) {
      // Set up SSH tunnel first, then create MySQL pool
      // Note: This is async, but we can't await in constructor
      // The pool will be created after SSH tunnel is established
      this.setupSSHTunnel(config).then(() => {
        this.createPool(config);
      }).catch((error: Error) => {
        console.error(`✗ Failed to set up SSH tunnel: ${error.message}`);
        // Don't throw here as it won't be caught - log and let connection attempts fail
      });
    } else {
      // Direct connection
      this.createPool(config);
    }
  }

  private async setupSSHTunnel(config: DatabaseConfig): Promise<void> {
    return new Promise((resolve, reject) => {
      if (!this.sshConfig) {
        reject(new Error('SSH config not provided'));
        return;
      }

      // Create a local server that will forward to remote MySQL
      this.localServer = createServer((localSocket) => {
        if (!this.sshClient) {
          localSocket.destroy();
          return;
        }

        this.sshClient.forwardOut(
          '127.0.0.1',
          0,
          '127.0.0.1',
          config.port,
          (err: Error | null | undefined, remoteSocket: ClientChannel | undefined) => {
            if (err || !remoteSocket) {
              localSocket.destroy();
              return;
            }
            localSocket.pipe(remoteSocket);
            remoteSocket.pipe(localSocket);
          }
        );
      });

      // Find an available local port and wait for it to be ready
      this.localServer.listen(0, '127.0.0.1', () => {
        const address = this.localServer!.address();
        if (address && typeof address === 'object') {
          this.localPort = address.port;
          console.log(`✓ Local tunnel server listening on port ${this.localPort}`);
        }
      });

      // Wait for the local server to be ready before connecting SSH
      this.localServer.on('listening', () => {
        // Store reference to ensure TypeScript knows it's not null
        const sshConfig = this.sshConfig;
        if (!sshConfig) {
          reject(new Error('SSH config lost'));
          return;
        }

        this.sshClient = new Client();
      
        this.sshClient.on('ready', () => {
          console.log(`✓ SSH tunnel established to ${sshConfig.host}`);
          resolve();
        });

        this.sshClient.on('error', (err: Error | undefined) => {
          const errorMessage = err?.message || 'Unknown SSH error';
          console.error(`✗ SSH connection error: ${errorMessage}`);
          if (this.localServer) {
            this.localServer.close();
          }
          reject(err || new Error(errorMessage));
        });

        // Connect via SSH
        const sshOptions: any = {
          host: sshConfig.host,
          port: sshConfig.port || 22,
          username: sshConfig.username,
        };

        if (sshConfig.password) {
          sshOptions.password = sshConfig.password;
        } else if (sshConfig.privateKey) {
          sshOptions.privateKey = sshConfig.privateKey;
        }

        this.sshClient.connect(sshOptions);
      });
    });
  }

  private createPool(config: DatabaseConfig): void {
    const poolConfig: any = {
      host: this.localPort ? '127.0.0.1' : config.host,
      port: this.localPort || config.port,
      user: config.user,
      password: config.password,
      database: config.database,
      waitForConnections: true,
      connectionLimit: 10,
      queueLimit: 0,
    };

    this.pool = mysql.createPool(poolConfig);

    // Test connection
    this.pool.getConnection()
      .then((connection) => {
        const connectionInfo = this.sshConfig 
          ? `${config.database} via SSH (${this.sshConfig.username}@${this.sshConfig.host})`
          : `${config.database} as ${config.user}@${config.host}`;
        console.log(`✓ Connected to database: ${connectionInfo}`);
        connection.release();
      })
      .catch((error) => {
        const connectionInfo = this.sshConfig 
          ? `${config.database} via SSH (${this.sshConfig.username}@${this.sshConfig.host})`
          : `${config.database} as ${config.user}@${config.host}`;
        console.error(`✗ Failed to connect to database: ${connectionInfo}`);
        console.error(`  Error: ${error.message}`);
        if (error.code === 'ER_ACCESS_DENIED_ERROR') {
          console.error('\n  💡 Tip: Check your database credentials in .env file');
          console.error('     Make sure the user has proper permissions and the password is correct.\n');
        }
      });
  }

  async close(): Promise<void> {
    await this.pool.end();
    if (this.localServer) {
      this.localServer.close();
    }
    if (this.sshClient) {
      this.sshClient.end();
      console.log('SSH tunnel closed');
    }
  }

  protected async getConnection(): Promise<PoolConnection> {
    return await this.pool.getConnection();
  }

  protected async releaseConnection(connection: PoolConnection): Promise<void> {
    connection.release();
  }
}

