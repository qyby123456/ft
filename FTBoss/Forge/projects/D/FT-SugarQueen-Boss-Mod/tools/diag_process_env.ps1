# Read another process's real environment block (PEB -> RTL_USER_PROCESS_PARAMETERS.Environment).
# Purpose: determine whether an ALREADY-RUNNING process actually has a given env var.
#          (Env vars are frozen at process start; changing the registry later does not affect it.)
# ASCII-only on purpose: Windows PowerShell 5.1 reads BOM-less .ps1 as ANSI/GBK.
# Usage: powershell -NoProfile -ExecutionPolicy Bypass -File tools\diag_process_env.ps1 -TargetPid 1234
param([Parameter(Mandatory=$true)][int]$TargetPid)

Add-Type -TypeDefinition @'
using System;
using System.Runtime.InteropServices;

public static class PebEnv {
    [StructLayout(LayoutKind.Sequential)]
    public struct PROCESS_BASIC_INFORMATION {
        public IntPtr Reserved1;
        public IntPtr PebBaseAddress;
        public IntPtr Reserved2_0;
        public IntPtr Reserved2_1;
        public IntPtr UniqueProcessId;
        public IntPtr Reserved3;
    }
    [DllImport("ntdll.dll")]
    public static extern int NtQueryInformationProcess(IntPtr h, int cls, ref PROCESS_BASIC_INFORMATION pbi, int len, out int retLen);
    [DllImport("kernel32.dll", SetLastError = true)]
    public static extern IntPtr OpenProcess(int access, bool inherit, int pid);
    [DllImport("kernel32.dll", SetLastError = true)]
    public static extern bool ReadProcessMemory(IntPtr h, IntPtr addr, byte[] buf, int size, out IntPtr read);
    [DllImport("kernel32.dll")]
    public static extern bool CloseHandle(IntPtr h);

    public static string ReadEnv(int pid) {
        const int PROCESS_QUERY_INFORMATION = 0x0400;
        const int PROCESS_VM_READ = 0x0010;
        IntPtr h = OpenProcess(PROCESS_QUERY_INFORMATION | PROCESS_VM_READ, false, pid);
        if (h == IntPtr.Zero) throw new Exception("OpenProcess failed, Win32Error=" + Marshal.GetLastWin32Error());
        try {
            var pbi = new PROCESS_BASIC_INFORMATION();
            int rl;
            int st = NtQueryInformationProcess(h, 0, ref pbi, Marshal.SizeOf(pbi), out rl);
            if (st != 0) throw new Exception("NtQueryInformationProcess status=0x" + st.ToString("X8"));
            byte[] p = new byte[8]; IntPtr br;
            // PEB->ProcessParameters : x64 offset 0x20
            if (!ReadProcessMemory(h, (IntPtr)(pbi.PebBaseAddress.ToInt64() + 0x20), p, 8, out br))
                throw new Exception("read ProcessParameters ptr failed");
            long pp = BitConverter.ToInt64(p, 0);
            // RTL_USER_PROCESS_PARAMETERS->Environment : x64 offset 0x80
            if (!ReadProcessMemory(h, (IntPtr)(pp + 0x80), p, 8, out br))
                throw new Exception("read Environment ptr failed");
            long env = BitConverter.ToInt64(p, 0);
            if (env == 0) return "";
            byte[] buf = new byte[512 * 1024];
            if (!ReadProcessMemory(h, (IntPtr)env, buf, buf.Length, out br))
                throw new Exception("read env block failed");
            int n = (int)br.ToInt64();
            return System.Text.Encoding.Unicode.GetString(buf, 0, n);
        } finally { CloseHandle(h); }
    }
}
'@

Write-Host "########## Process chain ##########"
$cur = $TargetPid
for ($i = 0; $i -lt 6 -and $cur -gt 0; $i++) {
    $p = Get-CimInstance Win32_Process -Filter "ProcessId=$cur" -ErrorAction SilentlyContinue
    if (-not $p) { Write-Host ("  " + (" " * $i) + "#$cur (gone)"); break }
    Write-Host ("  " + (" " * $i) + "#$($p.ProcessId)  $($p.Name)")
    if ($p.CommandLine) {
        $cl = $p.CommandLine
        Write-Host ("  " + (" " * $i) + "     " + $cl.Substring(0, [Math]::Min(140, $cl.Length)))
    }
    $cur = [int]$p.ParentProcessId
}

Write-Host ""
Write-Host "########## Target process environment ##########"
try {
    $env = [PebEnv]::ReadEnv($TargetPid)
    if (-not $env) { Write-Host "environment block is empty"; exit 0 }
    $vars = $env -split "`0" | Where-Object { $_ -match '=' }
    $hit = $vars | Where-Object { $_ -like 'PROXY_API_KEY=*' }
    if ($hit) {
        $val = $hit.Substring(14)
        $pfx = $val.Substring(0, [Math]::Min(4, $val.Length))
        Write-Host ("PROXY_API_KEY : PRESENT  len=" + $val.Length + "  prefix=" + $pfx + "****")
    } else {
        Write-Host "PROXY_API_KEY : *** ABSENT ***  (this process cannot see the key)"
    }
    Write-Host ""
    Write-Host "-- vars matching PROXY / OPENAI / CODEX / API_KEY --"
    $vars | Where-Object { $_ -match '^(PROXY|OPENAI|CODEX)' -or $_ -match 'API_KEY' } | ForEach-Object {
        $kv = $_ -split '=', 2
        if ($kv[1].Length -gt 8) { Write-Host ("  " + $kv[0] + " = len=" + $kv[1].Length + " prefix=" + $kv[1].Substring(0,4) + "****") }
        else { Write-Host ("  " + $kv[0] + " = " + $kv[1]) }
    }
    Write-Host ""
    Write-Host ("(total env vars in that process: " + $vars.Count + ")")
} catch {
    Write-Host ("READ FAILED: " + $_.Exception.Message)
    Write-Host "(if OpenProcess failed 5, elevation is required to read that process)"
}
