import sys

def calculate_averages(file_names):
    sum_TS = 0
    sum_TJ = 0
    count_TS = 0
    count_TJ = 0

    for file_name in file_names:
        with open(file_name, 'r') as file:
            for line in file:
                line = line.strip()
                if line.startswith('TS:'):
                    sum_TS += int(line.split(' ')[1])
                    count_TS += 1
                elif line.startswith('TJ:'):
                    sum_TJ += int(line.split(' ')[1])
                    count_TJ += 1

    return sum_TS / count_TS / 1000000, sum_TJ / count_TJ / 1000000

def main():
    if len(sys.argv) < 2:
        print('Please provide one or more file names')
        sys.exit(1)

    avg_TS, avg_TJ = calculate_averages(sys.argv[1:])
    print(f'Average TS: {avg_TS}' + "ms")
    print(f'Average TJ: {avg_TJ}' + "ms")

if __name__ == '__main__':
    main()
