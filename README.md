## Requirements

Java 17+

## Installation

POSIX based installation:
```bash
sudo mkdir /etc/aggressive-calendar-reminder
sudo chown $USER:$USER /etc/aggressive-calendar-reminder
cd /etc/aggressive-calendar-reminder
latest=$(curl --silent "https://api.github.com/repos/SiXoS/aggressive-calendar-reminder/releases/latest" | grep '"tag_name":' | sed -E 's/.*"([^"]+)".*/\1/')
wget -q https://github.com/SiXoS/aggressive-calendar-reminder/releases/download/$latest/acr
wget -q https://github.com/SiXoS/aggressive-calendar-reminder/releases/download/$latest/aggressive-calendar-reminder.jar
chmod +x acr
sudo touch /var/log/acr
sudo chown $USER:$USER /var/log/acr
```

Linux startup settings:
```bash
mkdir -p ~/.config/systemd/user
cd ~/.config/systemd/user
latest=$(curl --silent "https://api.github.com/repos/SiXoS/aggressive-calendar-reminder/releases/latest" | grep '"tag_name":' | sed -E 's/.*"([^"]+)".*/\1/')
wget -q https://github.com/SiXoS/aggressive-calendar-reminder/releases/download/$latest/aggressive-calendar-reminder.service
systemctl --user daemon-reload
systemctl --user enable aggressive-calendar-reminder.service
systemctl --user start aggressive-calendar-reminder.service
```

## Update

```bash
cd /etc/aggressive-calendar-reminder
latest=$(curl --silent "https://api.github.com/repos/SiXoS/aggressive-calendar-reminder/releases/latest" | grep '"tag_name":' | sed -E 's/.*"([^"]+)".*/\1/')
systemctl --user stop aggressive-calendar-reminder.service
wget -q https://github.com/SiXoS/aggressive-calendar-reminder/releases/download/$latest/aggressive-calendar-reminder.jar -O aggressive-calendar-reminder.jar
systemctl --user start aggressive-calendar-reminder.service
```
