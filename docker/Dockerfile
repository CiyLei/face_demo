FROM k4liber/face_recognition

RUN apk add --no-cache --update openssh
# passwd root
# 使用vim打开并修改配置文件 /etc/ssh/sshd_config
# 找到PermitRootLogin prohibit-password这一行，修改为PermitRootLogin yes
# ssh-keygen -t dsa -f /etc/ssh/ssh_host_dsa_key
# ssh-keygen -t rsa -f /etc/ssh/ssh_host_rsa_key
# 执行 /usr/sbin/sshd

RUN python3 -m pip install --no-cache-dir flask