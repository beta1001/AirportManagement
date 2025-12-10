"""
Generate charts (PNG) from airport telemetry CSV files.

Usage:
- python generate_charts.py                 -> uses telemetry_data.csv (single)
- python generate_charts.py A.csv           -> single file charts, tag based on filename
- python generate_charts.py A.csv B.csv     -> comparative charts (A vs B)

CSV columns expected (as exported by MainWindow):
 time, occupiedRunways, occupiedGates, taxiQueueSize, avgRunwayWait, avgGateWait, utilization, notifyAllCount

Note: User only uses notifyAll (no notify), so charts focus on notifyAll.
"""

import os
import sys
import pandas as pd
import matplotlib.pyplot as plt


def load_csv(path: str) -> pd.DataFrame:
    if not os.path.exists(path):
        raise FileNotFoundError(f"CSV not found: {path}")
    df = pd.read_csv(path)
    numeric_cols = [
        'time', 'occupiedRunways', 'occupiedGates', 'taxiQueueSize',
        'avgRunwayWait', 'avgGateWait', 'utilization', 'notifyAllCount'
    ]
    for col in numeric_cols:
        if col in df.columns:
            df[col] = pd.to_numeric(df[col], errors='coerce')
    return df


def safe_title(title: str) -> str:
    return title.replace(':', ' -')


def save_fig(fig, filename: str):
    fig.tight_layout()
    fig.savefig(filename, dpi=150)
    plt.close(fig)
    print(f"Saved: {filename}")


def charts_single(df: pd.DataFrame, tag: str = "telemetry"):
    # Average wait times
    fig1 = plt.figure(figsize=(10, 6))
    plt.plot(df['time'], df['avgRunwayWait'], label='Average Runway Wait (ms)')
    plt.plot(df['time'], df['avgGateWait'], label='Average Gate Wait (ms)', linestyle='--')
    plt.xlabel('Time (s)'); plt.ylabel('Wait (ms)')
    plt.title(safe_title(f'{tag}: Average Wait Times'))
    plt.legend(); plt.grid(True)
    save_fig(fig1, f'{tag}_wait_times.png')

    # Occupancy
    fig2 = plt.figure(figsize=(10, 6))
    plt.plot(df['time'], df['occupiedRunways'], label='Occupied Runways')
    plt.plot(df['time'], df['occupiedGates'], label='Occupied Gates')
    plt.xlabel('Time (s)'); plt.ylabel('Occupied Resources')
    plt.title(safe_title(f'{tag}: Occupancy'))
    plt.legend(); plt.grid(True)
    save_fig(fig2, f'{tag}_occupancy.png')

    # Utilization
    fig3 = plt.figure(figsize=(10, 6))
    plt.plot(df['time'], df['utilization'], color='tab:green')
    plt.xlabel('Time (s)'); plt.ylabel('Runway Utilization (0–1)')
    plt.title(safe_title(f'{tag}: Runway Utilization'))
    plt.grid(True)
    save_fig(fig3, f'{tag}_utilization.png')

    # Taxi queue
    fig4 = plt.figure(figsize=(10, 6))
    plt.plot(df['time'], df['taxiQueueSize'], color='tab:orange')
    plt.xlabel('Time (s)'); plt.ylabel('Taxi Queue Size (planes)')
    plt.title(safe_title(f'{tag}: Taxi Queue Size'))
    plt.grid(True)
    save_fig(fig4, f'{tag}_taxi_queue.png')

    # Final notifyAll bar
    if 'notifyAllCount' in df.columns and not df['notifyAllCount'].dropna().empty:
        final_notify_all = df['notifyAllCount'].dropna().iloc[-1]
        fig5 = plt.figure(figsize=(6, 4))
        plt.bar(['notifyAll'], [final_notify_all], color='tab:red')
        plt.ylabel('Count')
        plt.title(safe_title(f'{tag}: Final notifyAll Count'))
        for i, v in enumerate([final_notify_all]):
            plt.text(i, v, f'{int(v)}', ha='center', va='bottom')
        save_fig(fig5, f'{tag}_notify_all.png')


def charts_compare(df_a: pd.DataFrame, tag_a: str, df_b: pd.DataFrame, tag_b: str):
    # Average wait times comparison
    fig1 = plt.figure(figsize=(10, 6))
    plt.plot(df_a['time'], df_a['avgRunwayWait'], label=f'{tag_a} - Runway Wait')
    plt.plot(df_b['time'], df_b['avgRunwayWait'], label=f'{tag_b} - Runway Wait')
    plt.plot(df_a['time'], df_a['avgGateWait'], label=f'{tag_a} - Gate Wait', linestyle='--')
    plt.plot(df_b['time'], df_b['avgGateWait'], label=f'{tag_b} - Gate Wait', linestyle='--')
    plt.xlabel('Time (s)'); plt.ylabel('Wait (ms)')
    plt.title('Average Wait Times Comparison')
    plt.legend(); plt.grid(True)
    save_fig(fig1, 'compare_wait_times.png')

    # Utilization comparison
    fig2 = plt.figure(figsize=(10, 6))
    plt.plot(df_a['time'], df_a['utilization'], label=tag_a)
    plt.plot(df_b['time'], df_b['utilization'], label=tag_b)
    plt.xlabel('Time (s)'); plt.ylabel('Runway Utilization (0–1)')
    plt.title('Runway Utilization Comparison')
    plt.legend(); plt.grid(True)
    save_fig(fig2, 'compare_utilization.png')

    # Taxi queue comparison
    fig3 = plt.figure(figsize=(10, 6))
    plt.plot(df_a['time'], df_a['taxiQueueSize'], label=f'{tag_a} - Taxi Queue')
    plt.plot(df_b['time'], df_b['taxiQueueSize'], label=f'{tag_b} - Taxi Queue')
    plt.xlabel('Time (s)'); plt.ylabel('Taxi Queue Size (planes)')
    plt.title('Taxi Queue Size Comparison')
    plt.legend(); plt.grid(True)
    save_fig(fig3, 'compare_taxi_queue.png')

    # Final notifyAll comparison
    if 'notifyAllCount' in df_a.columns and 'notifyAllCount' in df_b.columns:
        v_a = df_a['notifyAllCount'].dropna().iloc[-1]
        v_b = df_b['notifyAllCount'].dropna().iloc[-1]
        fig4 = plt.figure(figsize=(6, 4))
        plt.bar([tag_a, tag_b], [v_a, v_b], color=['tab:red', 'tab:purple'])
        plt.ylabel('notifyAll Count')
        plt.title('Final notifyAll Count Comparison')
        for i, v in enumerate([v_a, v_b]):
            plt.text(i, v, f'{int(v)}', ha='center', va='bottom')
        save_fig(fig4, 'compare_notify_all.png')


def main():
    args = sys.argv[1:]
    if len(args) == 0:
        path = 'telemetry_data.csv'
        df = load_csv(path)
        tag = os.path.splitext(os.path.basename(path))[0]
        charts_single(df, tag)
        print('Done (single run).')
    elif len(args) == 1:
        path = args[0]
        df = load_csv(path)
        tag = os.path.splitext(os.path.basename(path))[0]
        charts_single(df, tag)
        print('Done (single run).')
    else:
        path_a, path_b = args[0], args[1]
        df_a = load_csv(path_a)
        df_b = load_csv(path_b)
        tag_a = os.path.splitext(os.path.basename(path_a))[0]
        tag_b = os.path.splitext(os.path.basename(path_b))[0]
        charts_compare(df_a, tag_a, df_b, tag_b)
        print('Done (comparison).')

if __name__ == '__main__':
    main()
