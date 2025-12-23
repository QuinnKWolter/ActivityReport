# ActivityReport - Node.js Implementation

Modern Node.js/TypeScript reimplementation of the ActivityReport service.

## Overview

ActivityReport is a web service that provides activity reporting functionality for educational applications. It aggregates and processes user activity data from multiple educational tools (QuizJet, SQLKnot, WebEx, Mastery Grids, etc.) and provides various reporting endpoints.

## Services

1. **RawActivity** - Exports raw activity data in CSV or JSON format
2. **ActivitySummary** - Generates per-student summary statistics
3. **GetSequences** - Creates activity sequences for pattern analysis
4. **FixTracking** - Generates SQL scripts to fix bad tracking data

## Setup

1. Install dependencies:
```bash
npm install
```

2. Create a `.env` file with database credentials:

**For direct database connection (local or remote without SSH):**
```env
UM2_DB_HOST=localhost
UM2_DB_PORT=3306
UM2_DB_USER=your_user
UM2_DB_PASSWORD=your_password
UM2_DB_NAME=um2

AGGREGATE_DB_HOST=localhost
AGGREGATE_DB_PORT=3306
AGGREGATE_DB_USER=your_user
AGGREGATE_DB_PASSWORD=your_password
AGGREGATE_DB_NAME=aggregate

PORT=3000
DELIMITER=,
```

**For SSH tunnel connection (remote database through SSH):**
```env
# SSH Tunnel Configuration
SSH_HOST=pawscomp2.sis.pitt.edu
SSH_USER=qkw3
SSH_PASSWORD=your_ssh_password
SSH_PORT=22

# MySQL Configuration (through SSH tunnel)
UM2_DB_HOST=127.0.0.1
UM2_DB_PORT=3306
UM2_DB_USER=qkw3
UM2_DB_PASSWORD=your_mysql_password
UM2_DB_NAME=um2

AGGREGATE_DB_HOST=127.0.0.1
AGGREGATE_DB_PORT=3306
AGGREGATE_DB_USER=qkw3
AGGREGATE_DB_PASSWORD=your_mysql_password
AGGREGATE_DB_NAME=aggregate

PORT=3000
DELIMITER=,
```

**Note:** When using SSH tunneling, set `UM2_DB_HOST` and `AGGREGATE_DB_HOST` to `127.0.0.1` since the connection goes through the SSH tunnel to localhost on the remote server.

3. Build the project:
```bash
npm run build
```

4. Run the server:
```bash
npm start
```

For development with auto-reload:
```bash
npm run dev
```

## API Endpoints

### RawActivity
`GET /ActivityReport?grp=group1,group2&[parameters]`

**Parameters:**
- `grp` (required): Comma-separated group IDs
- `header` (optional): Include header row (yes/no)
- `delimiter` (optional): CSV delimiter (default: comma)
- `fromDate` (optional): Start date filter (YYYY-MM-DD HH:mm:ss)
- `toDate` (optional): End date filter (YYYY-MM-DD HH:mm:ss)
- `filename` (optional): Output filename
- `svc` (optional): Include SVC field (yes/no)
- `allparameters` (optional): Include all parameters (yes/no)
- `removeUsr` (optional): Comma-separated users to exclude
- `excludeApp` (optional): Comma-separated app IDs to exclude
- `sessionate` (optional): Recalculate sessions
- `minthreshold` (optional): Session threshold in minutes (default: 90)
- `timelabels` (optional): Time labels (e.g., "short,long")
- `replaceexttimes` (optional): Replace extreme times with median
- `jsonOutput` (optional): Output as JSON (yes/no)
- `queryArchive` (optional): Query archive table (yes/no, default: yes)

### ActivitySummary
`GET /ActivitySummary?grp=group1&[parameters]`

**Parameters:**
- `grp` (required): Comma-separated group IDs
- `header` (optional): Include header row (yes/no)
- `filename` (optional): Output filename
- `usr` (optional): Comma-separated user IDs
- `fromDate` (optional): Start date filter
- `toDate` (optional): End date filter
- `timebins` (optional): Comma-separated Unix timestamps
- `sessionate` (optional): Recalculate sessions
- `minthreshold` (optional): Session threshold in minutes
- `queryArchive` (optional): Query archive table (yes/no, default: no)

### GetSequences
`GET /GetSequences?grp=group1&[parameters]`

**Parameters:**
- `grp` (required): Comma-separated group IDs
- `mode` (optional): Sequence mode (0=session, 1=question, 2=session/question, 3=topic)
- `include` (optional): Include filter (0=all, 1=questions, 2=examples, 3=both)
- `extended` (optional): Extended output format
- `pexspam` (optional): PEXSPAM format output
- `labelmap` (optional): Include label map
- `half` (optional): Return first (1) or second (2) half
- `header` (optional): Include header row
- `delimiter` (optional): CSV delimiter
- `filename` (optional): Output filename
- `usr` (optional): Comma-separated user IDs
- `fromDate` (optional): Start date filter
- `toDate` (optional): End date filter
- `svc` (optional): Include SVC field
- `allparameters` (optional): Include all parameters
- `removeUsr` (optional): Comma-separated users to exclude
- `timelabels` (optional): Time labels
- `replaceexttimes` (optional): Replace extreme times
- `markrepetition` (optional): Mark repetitions
- `markrepetitionseq` (optional): Mark sequence repetitions
- `queryArchive` (optional): Query archive table

### FixTracking
`GET /FixTracking?[archive]`

**Parameters:**
- `archive` (optional): Generate scripts for archive table

## Architecture

- **TypeScript** for type safety
- **Express.js** for the web framework
- **mysql2** for database connectivity
- **Modular design** with separate services, models, and utilities
