#!/usr/bin/env python3

import sys
from scapy.all import Ether, IP, UDP, Raw, sendp, get_if_list, get_if_addr, getmacbyip
import random

# ------------- Config -------------
IFACE = None  # otomatik tespit edilecek
SRC_IP = "10.0.0.123"  # spoofed IP
SRC_MAC = "aa:bb:cc:dd:ee:ff"  # spoofed MAC
DST_IP = "255.255.255.255"  # broadcast IP
DST_MAC = "ff:ff:ff:ff:ff:ff"
DST_PORT = 5005
SRC_PORT = random.randint(1024, 65535)
# ----------------------------------

def get_active_interface():
    for iface in get_if_list():
        try:
            ip = get_if_addr(iface)
            if not ip.startswith("127."):
                return iface
        except:
            continue
    return None

def spoof_send(message):
    ether = Ether(src=SRC_MAC, dst=DST_MAC)
    ip = IP(src=SRC_IP, dst=DST_IP)
    udp = UDP(sport=SRC_PORT, dport=DST_PORT)
    payload = Raw(load=message.encode())

    packet = ether / ip / udp / payload

    print(f"[+] Sending spoofed packet on interface {IFACE}")
    sendp(packet, iface=IFACE, verbose=False)

if __name__ == "__main__":
    if len(sys.argv) != 2:
        print(f"Usage: python {sys.argv[0]} \"<message>\"")
        sys.exit(1)

    IFACE = get_active_interface()
    if not IFACE:
        print("[-] No active network interface found.")
        sys.exit(1)

    msg = sys.argv[1]
    spoof_send(msg)
    print(f"[+] Spoofed message sent: {msg}")
