#!/usr/bin/env python3
"""Minimal Minecraft RCON client used to verify server commands.

Usage:
    python tools/rcon_exec.py --password pollution command one
    python tools/rcon_exec.py --password pollution "pollution aspects" "pollution get"
    python tools/rcon_exec.py --password pollution --file commands.txt

Enable RCON in run/server.properties first:
    enable-rcon=true
    rcon.port=25575
    rcon.password=pollution
"""

from __future__ import annotations

import argparse
import socket
import struct
import sys

SERVERDATA_AUTH = 3
SERVERDATA_AUTH_RESPONSE = 2
SERVERDATA_EXECCOMMAND = 2
SERVERDATA_RESPONSE_VALUE = 0


def _send(sock: socket.socket, request_id: int, packet_type: int, payload: str) -> None:
    body = struct.pack("<ii", request_id, packet_type) + payload.encode("utf-8") + b"\x00\x00"
    sock.sendall(struct.pack("<i", len(body)) + body)


def _recv(sock: socket.socket) -> tuple[int, int, str]:
    raw_length = _recv_exact(sock, 4)
    length = struct.unpack("<i", raw_length)[0]
    data = _recv_exact(sock, length)
    request_id, packet_type = struct.unpack("<ii", data[:8])
    payload = data[8:-2].decode("utf-8", errors="replace")
    return request_id, packet_type, payload


def _recv_exact(sock: socket.socket, count: int) -> bytes:
    buffer = b""
    while len(buffer) < count:
        chunk = sock.recv(count - len(buffer))
        if not chunk:
            raise ConnectionError("RCON connection closed")
        buffer += chunk
    return buffer


def run_command(sock: socket.socket, command: str) -> str:
    _send(sock, 1, SERVERDATA_EXECCOMMAND, command)
    # Servers may answer with an empty RESPONSE_VALUE before the real body.
    responses: list[str] = []
    while True:
        _, packet_type, payload = _recv(sock)
        if packet_type == SERVERDATA_RESPONSE_VALUE and payload:
            responses.append(payload)
            break
    return "".join(responses)


def main() -> int:
    if hasattr(sys.stdout, "reconfigure"):
        sys.stdout.reconfigure(encoding="utf-8")

    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--host", default="127.0.0.1")
    parser.add_argument("--port", type=int, default=25575)
    parser.add_argument("--password", required=True)
    parser.add_argument("--file", help="file with one command per line")
    parser.add_argument("commands", nargs="*")
    args = parser.parse_args()

    commands = list(args.commands)
    if args.file:
        with open(args.file, encoding="utf-8") as handle:
            commands.extend(line.strip() for line in handle if line.strip() and not line.startswith("#"))
    if not commands:
        print("no commands given", file=sys.stderr)
        return 1

    with socket.create_connection((args.host, args.port), timeout=15) as sock:
        _send(sock, 1, SERVERDATA_AUTH, args.password)
        request_id, packet_type, _ = _recv(sock)
        if packet_type == SERVERDATA_RESPONSE_VALUE:
            # Some servers send a dummy response before the auth response.
            request_id, packet_type, _ = _recv(sock)
        if request_id == -1:
            print("RCON authentication failed", file=sys.stderr)
            return 2

        for command in commands:
            print(f"> {command}")
            try:
                print(run_command(sock, command))
            except ConnectionError as error:
                print(f"connection error: {error}", file=sys.stderr)
                return 3
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
